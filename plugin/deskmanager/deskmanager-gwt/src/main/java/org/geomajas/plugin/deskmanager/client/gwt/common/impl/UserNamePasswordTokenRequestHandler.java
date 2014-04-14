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

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Window;
import org.geomajas.global.ExceptionDto;
import org.geomajas.gwt.client.command.TokenRequestHandler;
import org.geomajas.gwt.client.command.event.TokenChangedEvent;
import org.geomajas.gwt.client.command.event.TokenChangedHandler;
import org.geomajas.plugin.deskmanager.client.gwt.manager.service.DataCallback;

/**

 */
public class UserNamePasswordTokenRequestHandler implements TokenRequestHandler {


	@Override
	public void login(final TokenChangedHandler tokenChangedHandler) {
		   new UserNamePasswordWindow().askUserNameAndPassword(new Callback<UserNamePasswordModel, ExceptionDto>() {


			   @Override
			   public void onFailure(ExceptionDto reason) {

			   }

			   @Override
			   public void onSuccess(UserNamePasswordModel result) {
				   Window.alert("oejoejeeh!");
				   // check userName and password in database

				   // get profiles of user

				   // create token
				   String token = "dummyToken";
				   // save profiles to token

				   tokenChangedHandler.onTokenChanged(new TokenChangedEvent(token));
			   }
		   });
	}
}
