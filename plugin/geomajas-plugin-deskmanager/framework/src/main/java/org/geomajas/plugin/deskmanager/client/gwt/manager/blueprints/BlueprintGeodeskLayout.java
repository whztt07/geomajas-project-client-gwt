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
package org.geomajas.plugin.deskmanager.client.gwt.manager.blueprints;

import org.geomajas.plugin.deskmanager.client.gwt.manager.common.AbstractConfigurationLayout;
import org.geomajas.plugin.deskmanager.client.gwt.manager.common.GeodeskLayoutPanel;
import org.geomajas.plugin.deskmanager.client.gwt.manager.common.SaveButtonBar;
import org.geomajas.plugin.deskmanager.client.gwt.manager.events.BlueprintEvent;
import org.geomajas.plugin.deskmanager.client.gwt.manager.events.BlueprintSelectionHandler;
import org.geomajas.plugin.deskmanager.client.gwt.manager.events.Whiteboard;
import org.geomajas.plugin.deskmanager.client.gwt.manager.service.DataCallback;
import org.geomajas.plugin.deskmanager.client.gwt.manager.service.ManagerCommandService;
import org.geomajas.plugin.deskmanager.client.gwt.manager.util.GeodeskDtoUtil;
import org.geomajas.plugin.deskmanager.command.manager.dto.SaveBlueprintRequest;
import org.geomajas.plugin.deskmanager.configuration.client.GeodeskLayoutInfo;
import org.geomajas.plugin.deskmanager.domain.dto.BlueprintDto;

import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.widgets.events.ClickEvent;

/**
 * Define the layout of a geodesk.
 * 
 * @author Kristof Heirwegh
 */
public class BlueprintGeodeskLayout extends AbstractConfigurationLayout implements BlueprintSelectionHandler {

	private BlueprintDto blueprint;

	private GeodeskLayoutPanel layout;

	public BlueprintGeodeskLayout() {
		super();
		setMargin(5);
		SaveButtonBar buttonBar = new SaveButtonBar(this);
		addMember(buttonBar);

		// -------------------------------------------------
		layout = new GeodeskLayoutPanel();
		layout.setDisabled(true);
		layout.setWidth100();
		layout.setHeight100();
		layout.setOverflow(Overflow.AUTO);

		addMember(layout);
	}

	public void setBlueprint(BlueprintDto blueprint) {
		this.blueprint = blueprint;

		GeodeskLayoutInfo geodeskLayout = (GeodeskLayoutInfo) GeodeskDtoUtil.getApplicationClientWidgetInfo(blueprint)
				.get(GeodeskLayoutInfo.IDENTIFIER);

		if (geodeskLayout == null) {
			geodeskLayout = new GeodeskLayoutInfo();
		}
		layout.setGeodeskLayout(geodeskLayout);
		fireChangedHandler();
	}

	// -- SaveButtonBar events --------------------------------------------------------

	public boolean onEditClick(ClickEvent event) {
		layout.setDisabled(false);
		return true;
	}

	public boolean onSaveClick(ClickEvent event) {
		blueprint.getApplicationClientWidgetInfos().put(GeodeskLayoutInfo.IDENTIFIER, layout.getGeodeskLayout());

		ManagerCommandService.saveBlueprint(blueprint, SaveBlueprintRequest.SAVE_CLIENTWIDGETINFO);
		layout.setDisabled(true);
		return true;
	}

	public boolean onCancelClick(ClickEvent event) {
		setBlueprint(blueprint);
		layout.setDisabled(true);
		// Reload the blueprint
		ManagerCommandService.getBlueprint(blueprint.getId(), new DataCallback<BlueprintDto>() {

			public void execute(BlueprintDto result) {
				Whiteboard.fireEvent(new BlueprintEvent(result));
			}
		});
		return true;
	}

	public void onBlueprintSelectionChange(BlueprintEvent bpe) {
		setBlueprint(bpe.getBlueprint());
	}

	public boolean onResetClick(ClickEvent event) {
		blueprint.getApplicationClientWidgetInfos().remove(GeodeskLayoutInfo.IDENTIFIER);
		ManagerCommandService.saveBlueprint(blueprint, SaveBlueprintRequest.SAVE_CLIENTWIDGETINFO);
		layout.setDisabled(true);
		return true;
	}

	public boolean isDefault() {
		return !blueprint.getApplicationClientWidgetInfos().containsKey(GeodeskLayoutInfo.IDENTIFIER);
	}

}
