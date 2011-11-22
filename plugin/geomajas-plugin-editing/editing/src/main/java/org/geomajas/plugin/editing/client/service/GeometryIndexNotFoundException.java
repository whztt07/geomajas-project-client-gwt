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

package org.geomajas.plugin.editing.client.service;

/**
 * Exception that is thrown when a certain {@link GeometryIndex} could not be found within a certain
 * {@link org.geomajas.geometry.Geometry}. Used mainly in the {@link GeometryIndexService}.
 * 
 * @author Pieter De Graef
 */
public class GeometryIndexNotFoundException extends Exception {

	private static final long serialVersionUID = 100L;

	public GeometryIndexNotFoundException() {
		super();
	}

	public GeometryIndexNotFoundException(String message) {
		super(message);
	}
}