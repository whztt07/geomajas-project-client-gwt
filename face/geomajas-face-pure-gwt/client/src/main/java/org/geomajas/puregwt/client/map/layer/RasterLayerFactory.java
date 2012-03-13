/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2012 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */
package org.geomajas.puregwt.client.map.layer;

import org.geomajas.configuration.client.ClientRasterLayerInfo;
import org.geomajas.puregwt.client.map.MapEventBus;
import org.geomajas.puregwt.client.map.ViewPort;

/**
 * Gin factory for layers that are defined by {@link ClientRasterLayerInfo} configurations.
 * 
 * @author Jan De Moerloose
 * 
 */
public interface RasterLayerFactory {

	/**
	 * Create a layer for the specified configuration and view port.
	 * 
	 * @param clientRasterLayerInfo the configuration info
	 * @param viewPort the view port
	 * @param eventBus the map event bus
	 * @return the layer
	 */
	Layer<ClientRasterLayerInfo> create(ClientRasterLayerInfo clientRasterLayerInfo, ViewPort viewPort,
			MapEventBus eventBus);
}
