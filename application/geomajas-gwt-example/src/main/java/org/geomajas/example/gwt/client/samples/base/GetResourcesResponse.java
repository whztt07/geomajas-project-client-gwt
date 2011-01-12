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

package org.geomajas.example.gwt.client.samples.base;

import java.util.Map;

import org.geomajas.command.CommandResponse;

/**
 * <p>
 * Response object for the get resources command. Contains contents of resources on the class-path.
 * </p>
 * 
 * @author Pieter De Graef
 */
public class GetResourcesResponse extends CommandResponse {

	private static final long serialVersionUID = 153L;

	private Map<String, String> resources;

	// Constructors:

	public GetResourcesResponse() {
	}

	public GetResourcesResponse(Map<String, String> resources) {
		this.resources = resources;
	}

	// Getters and setters:

	public Map<String, String> getResources() {
		return resources;
	}

	public void setResources(Map<String, String> javaSource) {
		this.resources = javaSource;
	}
}
