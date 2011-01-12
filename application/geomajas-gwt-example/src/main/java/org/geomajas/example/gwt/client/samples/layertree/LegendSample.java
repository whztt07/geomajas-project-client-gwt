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

package org.geomajas.example.gwt.client.samples.layertree;

import org.geomajas.example.gwt.client.samples.base.SamplePanel;
import org.geomajas.gwt.client.controller.PanController;
import org.geomajas.gwt.client.map.layer.Layer;
import org.geomajas.example.gwt.client.samples.base.SamplePanelFactory;
import org.geomajas.example.gwt.client.samples.i18n.I18nProvider;
import org.geomajas.gwt.client.widget.Legend;
import org.geomajas.gwt.client.widget.MapWidget;

import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;

/**
 * <p>
 * Sample that shows how the legend reacts when layers become visible/invisible.
 * </p>
 * 
 * @author Pieter De Graef
 */
public class LegendSample extends SamplePanel {

	public static final String TITLE = "LegendSample";

	public static final SamplePanelFactory FACTORY = new SamplePanelFactory() {

		public SamplePanel createPanel() {
			return new LegendSample();
		}
	};

	public Canvas getViewPanel() {
		VLayout mainLayout = new VLayout();
		mainLayout.setWidth100();
		mainLayout.setHeight100();
		mainLayout.setMembersMargin(10);

		HLayout topLayout = new HLayout();
		topLayout.setMembersMargin(10);
		topLayout.setHeight(190);

		VLayout buttonLayout = new VLayout();
		buttonLayout.setMembersMargin(10);
		buttonLayout.setPadding(10);
		buttonLayout.setShowEdges(true);

		VLayout mapLayout = new VLayout();
		mapLayout.setShowEdges(true);
		final MapWidget map = new MapWidget("legendMap", "gwt-samples");
		map.setController(new PanController(map));
		mapLayout.addMember(map);

		IButton rasterButton = new IButton("Toggle Raster layer");
		rasterButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Layer<?> layer = map.getMapModel().getLayer("wmsLayer");
				layer.setVisible(!layer.isShowing());
			}
		});
		rasterButton.setWidth100();
		buttonLayout.addMember(rasterButton);

		IButton lineButton = new IButton("Toggle Line layer");
		lineButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Layer<?> layer = map.getMapModel().getLayer("rivers50mLayer");
				layer.setVisible(!layer.isShowing());
			}
		});
		lineButton.setWidth100();
		buttonLayout.addMember(lineButton);

		IButton polygonButton = new IButton("Toggle Polygon layer");
		polygonButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Layer<?> layer = map.getMapModel().getLayer("countries110mLayer");
				layer.setVisible(!layer.isShowing());
			}
		});
		polygonButton.setWidth100();
		buttonLayout.addMember(polygonButton);

		IButton pointButton = new IButton("Toggle Point layer");
		pointButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent event) {
				Layer<?> layer = map.getMapModel().getLayer("populatedPlaces110mLayer");
				layer.setVisible(!layer.isShowing());
			}
		});
		pointButton.setWidth100();
		buttonLayout.addMember(pointButton);

		VLayout legendLayout = new VLayout();
		legendLayout.setShowEdges(true);

		final Legend legend = new Legend(map.getMapModel());
		legend.setHeight100();
		legend.setWidth100();
		legendLayout.addMember(legend);

		topLayout.addMember(buttonLayout);
		topLayout.addMember(legendLayout);

		mainLayout.addMember(topLayout);
		mainLayout.addMember(mapLayout);
		return mainLayout;
	}

	public String getDescription() {
		return I18nProvider.getSampleMessages().legendDescription();
	}

	public String getSourceFileName() {
		return "classpath:org/geomajas/example/gwt/client/samples/layertree/LegendSample.txt";
	}

	public String[] getConfigurationFiles() {
		return new String[] { "WEB-INF/mapLegend.xml", "WEB-INF/layerLakes110m.xml",
				"WEB-INF/layerRivers50m.xml", "WEB-INF/layerPopulatedPlaces110m.xml", 
				"WEB-INF/layerWmsBluemarble.xml" };
	}

	public String ensureUserLoggedIn() {
		return "luc";
	}
}
