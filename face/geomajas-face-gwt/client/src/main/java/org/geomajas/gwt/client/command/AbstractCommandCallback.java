/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2011 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.gwt.client.command;

import org.geomajas.command.CommandResponse;
import org.geomajas.global.Api;

/**
 * Convenience class that implements {@link CommandExceptionCallback} and {@link CommunicationExceptionCallback} with
 * the default SC.say behavior.
 * 
 * @author Oliver May
 * @since 1.9.0
 */
@Api()
public abstract class AbstractCommandCallback implements CommandCallback, CommandExceptionCallback,
		CommunicationExceptionCallback {

	public void onCommunicationException(Throwable error) {
		GwtCommandDispatcher.getInstance().onCommunicationException(error);
	}

	public void onCommandException(CommandResponse response) {
		GwtCommandDispatcher.getInstance().onCommandException(response);
	}

}
