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
package org.geomajas.plugin.deskmanager.client.gwt.manager.security.view;

import org.geomajas.plugin.deskmanager.domain.security.dto.UserDto;

/**
 * Extension of {@link EditableLoadingView} interface for {@link UserDto}.
 *
 * @author Jan De Moerloose
 * @author Jan Venstermans
 */
public interface UserDetailView extends DetailView<UserDto> {

	void setPasswordValidationRule(String regex);

	/**
	 * Indicate to the view to adapt to user create settings.
	 * Ideally, these changes are temporary: after any action, go out of createUser mode.
	 */
	void setCreateUserMode();
}
