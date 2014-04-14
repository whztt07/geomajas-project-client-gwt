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
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Button;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Window;
import com.smartgwt.client.widgets.layout.VLayout;
import org.geomajas.global.ExceptionDto;
import org.geomajas.plugin.deskmanager.client.gwt.common.ProfileRequestCallback;
import org.geomajas.plugin.deskmanager.client.gwt.common.i18n.CommonMessages;
import org.geomajas.plugin.deskmanager.client.gwt.manager.service.DataCallback;
import org.geomajas.plugin.deskmanager.domain.security.dto.ProfileDto;
import org.geomajas.plugin.deskmanager.domain.security.dto.Role;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Default role implementation that asks for a role from a list of roles retrieved from the server.
 * 
 * @author Oliver May
 * 
 */
public class UserNamePasswordWindow {

	private static final CommonMessages MESSAGES = GWT.create(CommonMessages.class);

	/**
	 * Ask the user to select a role.
	 *
	 * @param callback the callback, called when a role is selected.
	 */
	public void askUserNameAndPassword(
			final Callback<UserNamePasswordModel, ExceptionDto> callback) {
		final Window winModal = new Window();
		winModal.setWidth(500);
		winModal.setHeight(300);
		winModal.setTitle("login");
		winModal.setShowMinimizeButton(false);
		winModal.setIsModal(true);
		winModal.setShowModalMask(true);
		winModal.centerInPage();
		winModal.setShowCloseButton(false);
		winModal.setZIndex(2000);

		VLayout layout = new VLayout();
		layout.setLayoutMargin(25);
		layout.setMembersMargin(10);


		Label label = new Label("UserName");
		label.setAutoHeight();
		layout.addMember(label);

		final TextBox text = new TextBox();
		layout.addMember(text);

		Label label2 = new Label("Password");
		label2.setAutoHeight();
		layout.addMember(label2);

		final PasswordTextBox passwordTextBox = new PasswordTextBox();
		layout.addMember(passwordTextBox);

		final Button button = new Button();
		button.setTitle("Login");
		button.setWidth100();
		button.addClickHandler(new com.smartgwt.client.widgets.events.ClickHandler() {

			public void onClick(com.smartgwt.client.widgets.events.ClickEvent event) {
				callback.onSuccess(new UserNamePasswordModel(text.getText(), passwordTextBox.getText()));
				winModal.destroy();
			}
		});

		winModal.addItem(layout);
		winModal.show();

	}

}
