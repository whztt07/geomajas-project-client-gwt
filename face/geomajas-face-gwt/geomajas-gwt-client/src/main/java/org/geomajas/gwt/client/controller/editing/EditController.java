/*
 * This file is part of Geomajas, a component framework for building
 * rich Internet applications (RIA) with sophisticated capabilities for the
 * display, analysis and management of geographic information.
 * It is a building block that allows developers to add maps
 * and other geographic data capabilities to their web applications.
 *
 * Copyright 2008-2010 Geosparc, http://www.geosparc.com, Belgium
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.geomajas.gwt.client.controller.editing;

import org.geomajas.gwt.client.controller.AbstractSnappingController;
import org.geomajas.gwt.client.map.feature.FeatureTransaction;
import org.geomajas.gwt.client.map.feature.TransactionGeomIndex;
import org.geomajas.gwt.client.spatial.WorldViewTransformer;
import org.geomajas.gwt.client.spatial.geometry.Geometry;
import org.geomajas.gwt.client.widget.MapWidget;
import org.geomajas.gwt.client.widget.MapWidget.RenderGroup;
import org.geomajas.gwt.client.widget.MapWidget.RenderStatus;

import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.menu.Menu;

/**
 * Basic template for a controller that handles the editing of certain types of geometries.
 * 
 * @author Pieter De Graef
 */
public abstract class EditController extends AbstractSnappingController {

	/**
	 * When editing geometries, 2 editing modes exist: one in which you constantly add new points to the geometry
	 * (INSERT_MODE), and one where you are free to move points, add or remove points etc (DRAG_MODE). This is the
	 * definition of both modes.
	 */
	public static enum EditMode {
		DRAG_MODE, INSERT_MODE
	}

	/**
	 * Reference to a parent editing controller. Editing controllers support a hierarchical structure where a parent
	 * delegates to the correct child EditController, depending on the circumstances.
	 */
	protected EditController parent;

	/**
	 * The current active editing modus.
	 */
	private EditMode editMode;

	/**
	 * The current active context menu on the map.
	 */
	protected Menu menu;

	/**
	 * Definition of the label that displays geometric information of the geometry that's currently being edited.
	 */
	protected GeometricInfoLabel infoLabel;

	// -------------------------------------------------------------------------
	// Constructor
	// -------------------------------------------------------------------------

	/**
	 * Protected and only constructor. Extending classes must use this constructor.
	 * 
	 * @param mapWidget
	 *            Reference to the map widget on which editing is in progress.
	 * @param parent
	 *            The parent editing controller, or null if there is none.
	 */
	protected EditController(MapWidget mapWidget, EditController parent) {
		super(mapWidget);
		this.parent = parent;
	}

	// -------------------------------------------------------------------------
	// Class specific functions:
	// -------------------------------------------------------------------------

	/**
	 * Return a context menu specific for this editing controller instance.
	 */
	public abstract Menu getContextMenu();

	/**
	 * When creating a new feature, the controller must specify through a <code>TransactionGeomIndex</code> object,
	 * where to begin adding coordinates in the geometry. The contents of this index will depend on the type of
	 * geometric object the edit controller supports.
	 * 
	 * @return
	 */
	public abstract TransactionGeomIndex getGeometryIndex();

	/**
	 * Set a new index at which inserting of points should happen.
	 * 
	 * @param geometryIndex
	 */
	public abstract void setGeometryIndex(TransactionGeomIndex geometryIndex);

	/**
	 * Clean up all graphical stuff.
	 */
	public abstract void cleanup();

	/**
	 * Show an overview of geometric attributes of the geometry that's being edited.
	 */
	public void showGeometricInfo() {
		FeatureTransaction ft = getFeatureTransaction();
		if (infoLabel == null && ft != null && ft.getNewFeatures() != null && ft.getNewFeatures().length > 0) {
			infoLabel = new GeometricInfoLabel(getTransformer());
			infoLabel.addClickHandler(new DestroyLabelInfoOnClick());
			infoLabel.setGeometry(ft.getNewFeatures()[0].getGeometry());
			infoLabel.animateMove(mapWidget.getWidth() - 155, 10);
		}
	}

	/**
	 * Update the overview of geometric attributes of the geometry that's being edited.
	 */
	public void updateGeometricInfo() {
		FeatureTransaction ft = getFeatureTransaction();
		if (infoLabel != null && ft != null && ft.getNewFeatures() != null && ft.getNewFeatures().length > 0) {
			infoLabel.setGeometry(ft.getNewFeatures()[0].getGeometry());
		}
	}

	/**
	 * Hide the overview of geometric attributes.
	 */
	public void hideGeometricInfo() {
		if (infoLabel != null) {
			infoLabel.destroy();
			infoLabel = null;
		}
	}

	// -------------------------------------------------------------------------
	// Getters and setters:
	// -------------------------------------------------------------------------

	public EditController getParent() {
		return parent;
	}

	public EditMode getEditMode() {
		return editMode;
	}

	public void setEditMode(EditMode editMode) {
		this.editMode = editMode;
	}

	public GeometricInfoLabel getInfoLabel() {
		return infoLabel;
	}

	// -------------------------------------------------------------------------
	// GraphicsController implementation:
	// -------------------------------------------------------------------------

	public void onActivate() {
		menu = getContextMenu();
		mapWidget.setContextMenu(menu);
	}

	public void onDeactivate() {
		if (getFeatureTransaction() != null) {
			mapWidget.render(getFeatureTransaction(), RenderGroup.SCREEN, RenderStatus.DELETE);
			mapWidget.getMapModel().getFeatureEditor().stopEditing();
		}
		if (menu != null) {
			menu.destroy();
			menu = null;
			mapWidget.setContextMenu(null);
		}
		hideGeometricInfo();
		cleanup();
	}

	// -------------------------------------------------------------------------
	// Protected helper methods:
	// -------------------------------------------------------------------------

	/**
	 * Helper method to the FeatureTransaction.
	 */
	protected FeatureTransaction getFeatureTransaction() {
		return mapWidget.getMapModel().getFeatureEditor().getFeatureTransaction();
	}

	// -------------------------------------------------------------------------
	// Private inner classes:
	// -------------------------------------------------------------------------

	/**
	 * Label that displays some basic geometric information, such as length and area.
	 * 
	 * @author Pieter De Graef
	 */
	private class GeometricInfoLabel extends Label {

		private WorldViewTransformer transformer;

		private Geometry geometry;

		// Constructors:

		protected GeometricInfoLabel(WorldViewTransformer transformer) {
			setParentElement(mapWidget);
			this.transformer = transformer;
			setValign(VerticalAlignment.TOP);
			setShowEdges(true);
			setWidth(145);
			setPadding(3);
			setLeft(mapWidget.getWidth() - 155);
			setTop(-80);
			setBackgroundColor("#FFFFFF");
			setAnimateTime(500);
		}

		// Getters and setters:

		public void setGeometry(Geometry geometry) {
			if (geometry != null) {
				this.geometry = transformer.viewToWorld(geometry);
			}
			updateContents();
		}

		// Private methods:

		private void updateContents() {
			String contents = "<b>Geometric info:</b><br/><br/>";
			if (geometry == null) {
				setContents(contents + "No geometry to display");
			} else {
				contents += "Area: " + (float) geometry.getArea() + "<br/>";
				contents += "Length: " + (float) geometry.getLength() + "<br/>";
				contents += "Nr points: " + geometry.getNumPoints();
				setContents(contents);
			}
		}
	}

	/**
	 * ???
	 */
	private class DestroyLabelInfoOnClick implements ClickHandler {

		public void onClick(ClickEvent event) {
			infoLabel.destroy();
			infoLabel = null;
		}
	}
}
