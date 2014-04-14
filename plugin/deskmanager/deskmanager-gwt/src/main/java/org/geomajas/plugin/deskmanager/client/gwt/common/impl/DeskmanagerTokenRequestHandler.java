/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2014 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.plugin.deskmanager.client.gwt.common.impl;

import com.google.gwt.i18n.client.LocaleInfo;
import org.geomajas.gwt.client.command.CommandCallback;
import org.geomajas.gwt.client.command.GwtCommand;
import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt.client.command.TokenRequestHandler;
import org.geomajas.gwt.client.command.event.TokenChangedEvent;
import org.geomajas.gwt.client.command.event.TokenChangedHandler;
import org.geomajas.plugin.deskmanager.client.gwt.common.ProfileRequestCallback;
import org.geomajas.plugin.deskmanager.command.security.dto.RetrieveRolesRequest;
import org.geomajas.plugin.deskmanager.command.security.dto.RetrieveRolesResponse;
import org.geomajas.plugin.deskmanager.domain.security.dto.ProfileDto;
import org.geomajas.plugin.deskmanager.domain.security.dto.Role;

import java.util.Map;

/**
 * Token request handler for the deskmanager. This handler will request a list of roles from the server, and let the
 * user choose the correct one.
 * <p/>
 * This implementation presumes that the user has already logged in trough another authentication way (reverse proxy,
 * sso,...) so that only a specific role for the application has to be chosen.
 *
 * @author Oliver May
 */
public class DeskmanagerTokenRequestHandler implements org.geomajas.gwt.client.command.TokenRequestHandler {

	private TokenRequestHandler fallbackTokenRequestHandler;
	private String geodeskId;

	protected String token;

	protected ProfileDto profile;

	private String securityToken;


	/**
	 * Create a deskmanaer token request handler. This will show a list of profiles valid for the selected
	 * geodesk/manager application. If needed it will call a fallbackTokenRequestHandler handler to request a valid security token.
	 *
	 * @param geodeskId                   the geodesk to open.
	 * @param fallbackTokenRequestHandler the fallbackTokenRequestHandler handler to retrieve a token from
	 */
	public DeskmanagerTokenRequestHandler(String geodeskId, TokenRequestHandler fallbackTokenRequestHandler) {
		this.geodeskId = geodeskId;
		this.fallbackTokenRequestHandler = fallbackTokenRequestHandler;
	}

	@Override
	public void login(final TokenChangedHandler tokenChangedHandler) {
		doLogin(tokenChangedHandler, fallbackTokenRequestHandler);
	}

	private void doLogin(final TokenChangedHandler tokenChangedHandler,
						 final TokenRequestHandler fallbackTokenRequestHandler) {
		RetrieveRolesRequest request = new RetrieveRolesRequest();
		request.setGeodeskId(geodeskId);
		request.setLocale(LocaleInfo.getCurrentLocale().getLocaleName());
		request.setSecurityToken(securityToken);

		GwtCommand command = new GwtCommand(RetrieveRolesRequest.COMMAND);
		command.setCommandRequest(request);
		GwtCommandDispatcher.getInstance().execute(command, new CommandCallback<RetrieveRolesResponse>() {

			public void execute(RetrieveRolesResponse response) {
				//Guest role, proceed
				Map.Entry<String, ProfileDto> guest = null;
				for (Map.Entry<String, ProfileDto> role : response.getRoles().entrySet()) {
					if (role.getValue().getRole().equals(Role.GUEST)) {
						guest = role;
						tokenChangedHandler.onTokenChanged(new TokenChangedEvent(token));
						return;
					}
				}

				// If only one role, use single role
				if (response.getRoles().size() == 1) {
					for (Map.Entry<String, ProfileDto> role : response.getRoles().entrySet()) {
						tokenChangedHandler.onTokenChanged(new TokenChangedEvent(token));
					}
				// If more than one role; ask for correct role
				} else if (response.getRoles().size() > 0) {
					RolesWindow roleSelectWindow = new RolesWindow(RetrieveRolesRequest.MANAGER_ID.equals(geodeskId));
					roleSelectWindow.askRoleWindow(response.getRoles(), new ProfileRequestCallback() {
						@Override
						public void onTokenChanged(String token, ProfileDto profile) {
							DeskmanagerTokenRequestHandler.this.token = token;
							DeskmanagerTokenRequestHandler.this.profile = profile;
							tokenChangedHandler.onTokenChanged(new TokenChangedEvent(token));
						}
					});
				// If no roles, try fallback.
				} else if (fallbackTokenRequestHandler != null) {
					fallbackTokenRequestHandler.login(new TokenChangedHandler() {
						@Override
						public void onTokenChanged(TokenChangedEvent event) {
							DeskmanagerTokenRequestHandler.this.token = event.getToken();
							doLogin(tokenChangedHandler, null);
						}
					});
				// Giving up, unauthorized.
				} else {
					new UnauthorizedWindow().show();
				}
			}
		});
	}


	/**
	 * Get the token for the current active role.
	 *
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Get the profile for the current active role.
	 *
	 * @return the profile
	 */
	public ProfileDto getProfile() {
		return profile;
	}

}
