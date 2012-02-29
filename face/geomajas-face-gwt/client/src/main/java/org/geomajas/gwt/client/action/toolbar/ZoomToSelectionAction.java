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

package org.geomajas.gwt.client.action.toolbar;

import java.util.ArrayList;
import java.util.List;

import org.geomajas.gwt.client.action.ToolbarAction;
import org.geomajas.gwt.client.i18n.I18nProvider;
import org.geomajas.gwt.client.map.feature.Feature;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.gwt.client.util.WidgetLayout;
import org.geomajas.gwt.client.widget.MapWidget;

import com.smartgwt.client.widgets.events.ClickEvent;

/**
 * Zoom to the current selection.
 * 
 * @author Frank Wynants
 * @author An Buyle
 */
public class ZoomToSelectionAction extends ToolbarAction {

	private MapWidget mapWidget;

	public ZoomToSelectionAction(MapWidget mapWidget) {
		super(WidgetLayout.iconZoomSelection, I18nProvider.getToolbar().zoomToSelectionTitle(),
				I18nProvider.getToolbar().zoomToSelectionTooltip());
		this.mapWidget = mapWidget;
	}

	/**
	 * Zoom to the selected features. No need to have an active layer.
	 * Selected features can belong to multiple layers
	 */
	public void onClick(ClickEvent clickEvent) {
		List<Feature> features = new ArrayList<Feature>();
 		for (VectorLayer layer : mapWidget.getMapModel().getVectorLayers()) {
			features.addAll(layer.getSelectedFeatureValues());
		}
		// only zoom when there where really some items selected
		if (features.size() > 0) {
			mapWidget.getMapModel().zoomToFeatures(features);
		}

	}
}
