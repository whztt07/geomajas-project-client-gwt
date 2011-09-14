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

package org.geomajas.plugin.staticsecurity.gwt.example.client;

import org.geomajas.gwt.client.command.GwtCommandDispatcher;
import org.geomajas.gwt.client.gfx.style.ShapeStyle;
import org.geomajas.gwt.client.map.event.MapModelEvent;
import org.geomajas.gwt.client.map.event.MapModelHandler;
import org.geomajas.gwt.client.util.WidgetLayout;
import org.geomajas.gwt.client.widget.LayerTree;
import org.geomajas.gwt.client.widget.Legend;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.gwt.client.widget.OverviewMap;
import org.geomajas.gwt.client.widget.Toolbar;

import com.google.gwt.core.client.EntryPoint;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.layout.VLayout;
import org.geomajas.plugin.staticsecurity.client.StaticSecurityTokenRequestHandler;

/**
 * Entry point and main class for GWT application. This class defines the layout and functionality of this application.
 * 
 * @author geomajas-gwt-archetype
 */
public class Application implements EntryPoint {

	public static final String APPLICATION_LABEL = "Geomajas GWT: staticsecurity example";
	public static final String APPLICATION_TITLE_STYLE = "appTitle";

	private OverviewMap overviewMap;

	private Legend legend;

	public void onModuleLoad() {
		GwtCommandDispatcher.getInstance().setTokenRequestHandler(new StaticSecurityTokenRequestHandler(
				"Possible users are 'luc' and 'marino'. The password is the same as the login."));

		VLayout mainLayout = new VLayout();
		mainLayout.setWidth100();
		mainLayout.setHeight100();
		mainLayout.setBackgroundColor("#A0A0A0");

		HLayout layout = new HLayout();
		layout.setWidth100();
		layout.setHeight100();
		layout.setMembersMargin(WidgetLayout.marginLarge);
		layout.setMargin(WidgetLayout.marginLarge);

		// ---------------------------------------------------------------------
		// Create the left-side (map and tabs):
		// ---------------------------------------------------------------------
		final MapWidget map = new MapWidget("mapMain", "app");
		final Toolbar toolbar = new Toolbar(map);
		toolbar.setButtonSize(Toolbar.BUTTON_SIZE_BIG);
		toolbar.setBackgroundColor("#647386");
		toolbar.setBackgroundImage("");
		toolbar.setBorder("0px");

		map.getMapModel().addMapModelHandler(new MapModelHandler() {

			public void onMapModelChange(MapModelEvent event) {
				Label title = new Label(APPLICATION_LABEL);
				title.setStyleName(APPLICATION_TITLE_STYLE);
				title.setWidth("*");
				toolbar.addFill();
				toolbar.addMember(title);
			}
		});

		VLayout mapLayout = new VLayout();
		mapLayout.addMember(toolbar);
		mapLayout.addMember(map);
		mapLayout.setHeight("65%");

		VLayout leftLayout = new VLayout();
		leftLayout.setBorder("10px solid #777777");
		leftLayout.setStyleName("round_corner");
		leftLayout.addMember(mapLayout);

		layout.addMember(leftLayout);

		// Add a search panel to the top-right of the map:
		//SearchPanel searchPanel = new SearchPanel(map.getMapModel(), mapLayout);
		//mapLayout.addChild(searchPanel);
		
		// ---------------------------------------------------------------------
		// Create the right-side (overview map, layer-tree, legend):
		// ---------------------------------------------------------------------
		final SectionStack sectionStack = new SectionStack();
		sectionStack.setBorder("10px solid #777777");
		sectionStack.setStyleName("round_corner");
		sectionStack.setVisibilityMode(VisibilityMode.MULTIPLE);
		sectionStack.setCanReorderSections(true);
		sectionStack.setCanResizeSections(false);
		sectionStack.setSize("250px", "100%");

		// Overview map layout:
		SectionStackSection section1 = new SectionStackSection("Overview map");
		section1.setExpanded(true);
		overviewMap = new OverviewMap("mapOverview", "app", map, false, true);
		overviewMap.setTargetMaxExtentRectangleStyle(new ShapeStyle("#888888", 0.3f, "#666666", 0.75f, 2));
		overviewMap.setRectangleStyle(new ShapeStyle("#6699FF", 0.3f, "#6699CC", 1f, 2));
		section1.addItem(overviewMap);
		sectionStack.addSection(section1);

		// LayerTree layout:
		SectionStackSection section2 = new SectionStackSection("Layer tree");
		section2.setExpanded(true);
		LayerTree layerTree = new LayerTree(map);
		section2.addItem(layerTree);
		sectionStack.addSection(section2);

		// Legend layout:
		SectionStackSection section3 = new SectionStackSection("Legend");
		section3.setExpanded(true);
		legend = new Legend(map.getMapModel());
		legend.setBackgroundColor("#FFFFFF");
		section3.addItem(legend);
		sectionStack.addSection(section3);

		// Putting the right side layouts together:
		layout.addMember(sectionStack);

		// ---------------------------------------------------------------------
		// Finally draw everything:
		// ---------------------------------------------------------------------
		mainLayout.addMember(layout);
		mainLayout.draw();

		// Install a loading screen.
		// This only works if the application initially shows a map with at least 1 vector layer:
		// LoadingScreen loadScreen = new LoadingScreen(map, "Geomajas staticsecurity example application");
		// loadScreen.draw();

		// Then initialize:
		initialize();
	}

	private void initialize() {
		legend.setHeight(200);
		overviewMap.setHeight(200);
	}
}
