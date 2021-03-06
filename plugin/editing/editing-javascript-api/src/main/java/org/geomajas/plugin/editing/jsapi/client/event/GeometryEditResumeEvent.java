/*
 * This is part of Geomajas, a GIS framework, http://www.geomajas.org/.
 *
 * Copyright 2008-2015 Geosparc nv, http://www.geosparc.com/, Belgium.
 *
 * The program is available in open source according to the GNU Affero
 * General Public License. All contributions in this program are covered
 * by the Geomajas Contributors License Agreement. For full licensing
 * details, see LICENSE.txt in the project root.
 */

package org.geomajas.plugin.editing.jsapi.client.event;

import org.geomajas.annotation.Api;
import org.geomajas.geometry.Geometry;
import org.geomajas.plugin.jsapi.client.event.JsEvent;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

/**
 * Event that reports the editing of a geometry has resumed.
 * 
 * @author Jan De Moerloose
 * @since 1.0.0
 */
@Api(allMethods = true)
@Export
@ExportPackage("org.geomajas.plugin.editing.event")
public class GeometryEditResumeEvent extends JsEvent<GeometryEditResumeHandler> implements Exportable {

	private Geometry geometry;

	/**
	 * Main constructor.
	 * 
	 * @param geometry geometry
	 */
	public GeometryEditResumeEvent(Geometry geometry) {
		this.geometry = geometry;
	}

	/**
	 * {@inheritDoc}
	 */
	public Class<GeometryEditResumeHandler> getType() {
		return GeometryEditResumeHandler.class;
	}

	protected void dispatch(GeometryEditResumeHandler handler) {
		handler.onGeometryEditResume(this);
	}

	/**
	 * Get the geometry that will be edited.
	 * 
	 * @return The geometry that is to be edited.
	 */
	public Geometry getGeometry() {
		return geometry;
	}
}