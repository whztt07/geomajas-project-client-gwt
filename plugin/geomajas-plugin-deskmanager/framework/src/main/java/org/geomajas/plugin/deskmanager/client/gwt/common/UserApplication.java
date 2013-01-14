/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2013 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.plugin.deskmanager.client.gwt.common;

import org.geomajas.annotation.FutureApi;
import org.geomajas.configuration.client.ClientApplicationInfo;
import org.geomajas.configuration.client.ClientWidgetInfo;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.plugin.deskmanager.client.gwt.geodesk.widget.event.UserApplicationHandler;

import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.widgets.layout.Layout;

/**
 * Interface for the deskmanager applications.
 * 
 * @author Oliver May
 *
 */
@FutureApi
public interface UserApplication {

	/**
	 * This method acts as the entrypoint for a deskmanager user application. When all initialization is done, this
	 * method gets called. The returing layout is the main contentpane for the user application.
	 * 
	 * Be sure that setGeodeskId() and setClientApplicationInfo() are set.
	 */
	Layout loadGeodesk();
	
	/**
	 * Set the application Id.
	 * @param applicationId
	 */
	void setApplicationId(String applicationId);
	
	
	/**
	 * Set the clientapplicationinfo.
	 * @param applicationInfo
	 */
	void setClientApplicationInfo(ClientApplicationInfo applicationInfo);
	
	/**
	 * Provide access to the user application's main mapwidget.
	 * 
	 * @return the main mapwidget.
	 */
	MapWidget getMainMapWidget();

	/**
	 * Provide access to the user application's overview mapwidget.
	 * 
	 * @return the main mapwidget. Null is no overview map is present in this user application.
	 */
	MapWidget getOverviewMapWidget();

	
	/**
	 * Event when the user application is initialised.
	 * 
	 * @return the main mapwidget.
	 */
	HandlerRegistration addUserApplicationLoadedHandler(UserApplicationHandler uah);

	/**
	 * Fire the user application event to all handlers.
	 * 
	 */
	void fireUserApplicationEvent();
	
	/**
	 * Get name of the geodesk.
	 * 
	 * @return the name of the geodesk
	 */
	String getName();

	/**
	 * Get banner of the geodesk, for display on the loading screen.
	 * 
	 * @return the banner of the geodesk
	 */
	String getBannerUrl();
	
	/**
	 * Chieck if this UserApplication supports a widget configuration.
	 * 
	 * @param cwi
	 * @return true if this application supports configuration of this type of ClientWidgetInfo
	 */
	boolean supportsWidgetConfiguration(ClientWidgetInfo cwi);
	
	String getClientApplicationKey();
	String getClientApplicationName();
	
	boolean hasLayerTree();
	
}
