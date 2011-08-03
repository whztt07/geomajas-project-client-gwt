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
package org.geomajas.plugin.rasterizing.layer;

import java.awt.Rectangle;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.geomajas.configuration.AssociationAttributeInfo;
import org.geomajas.configuration.AttributeInfo;
import org.geomajas.configuration.FeatureInfo;
import org.geomajas.configuration.FeatureStyleInfo;
import org.geomajas.configuration.GeometryAttributeInfo;
import org.geomajas.configuration.PrimitiveAttributeInfo;
import org.geomajas.configuration.VectorLayerInfo;
import org.geomajas.configuration.client.ClientLayerInfo;
import org.geomajas.configuration.client.ClientVectorLayerInfo;
import org.geomajas.geometry.Crs;
import org.geomajas.global.ExceptionCode;
import org.geomajas.global.GeomajasException;
import org.geomajas.layer.VectorLayer;
import org.geomajas.layer.VectorLayerService;
import org.geomajas.layer.feature.InternalFeature;
import org.geomajas.plugin.rasterizing.api.LayerFactory;
import org.geomajas.plugin.rasterizing.api.StyleFactoryService;
import org.geomajas.plugin.rasterizing.command.dto.VectorLayerRasterizingInfo;
import org.geomajas.service.ConfigurationService;
import org.geomajas.service.DtoConverterService;
import org.geomajas.service.FilterService;
import org.geomajas.service.GeoService;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.MetaBufferEstimator;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleAttributeExtractor;
import org.jboss.serial.io.JBossObjectOutputStream;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Envelope;

/**
 * This factory creates a GeoTools layer that is capable of writing vector layers.
 * 
 * @author Jan De Moerloose
 */
@Component
public class VectorLayerFactory implements LayerFactory {

	private static final String STYLE_INDEX_ATTRIBUTE_NAME = "geomajas_style";

	@Autowired
	private VectorLayerService vectorLayerService;

	@Autowired
	private GeoService geoService;

	@Autowired
	private FilterService filterService;

	@Autowired
	private StyleFactoryService styleFactoryService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private DtoConverterService dtoConverterService;

	/** Tolerance used to compare doubles for equality */
	private static final double TOLERANCE = 1e-6;

	public boolean canCreateLayer(MapContext mapContext, ClientLayerInfo clientLayerInfo) {
		return clientLayerInfo instanceof ClientVectorLayerInfo;
	}

	public Layer createLayer(MapContext mapContext, ClientLayerInfo clientLayerInfo) throws GeomajasException {
		if (!(clientLayerInfo instanceof ClientVectorLayerInfo)) {
			throw new IllegalStateException(
					"VectorLayerFactory.createLayer() should only be called using ClientVectorLayerInfo");
		}
		ClientVectorLayerInfo vectorInfo = (ClientVectorLayerInfo) clientLayerInfo;
		VectorLayerRasterizingInfo extraInfo = (VectorLayerRasterizingInfo) vectorInfo
				.getWidgetInfo(VectorLayerRasterizingInfo.WIDGET_KEY);
		ReferencedEnvelope areaOfInterest = mapContext.getAreaOfInterest();
		VectorLayer layer = configurationService.getVectorLayer(vectorInfo.getServerLayerId());
		// need to clone the extra info object before changing it !
		VectorLayerRasterizingInfo copy = cloneInfo(extraInfo);
		// we now replace the style filters by simple filters on an artificial extra style attribute
		for (FeatureStyleInfo style : copy.getStyle().getFeatureStyles()) {
			style.setFormula(STYLE_INDEX_ATTRIBUTE_NAME + " = " + style.getIndex());
		}
		// create the style
		Style style = styleFactoryService.createStyle(layer, copy);
		// estimate the buffer
		MetaBufferEstimator estimator = new MetaBufferEstimator();
		estimator.visit(style);
		int bufferInPixels = estimator.getBuffer();
		// expand area to include buffer
		Rectangle tileInpix = mapContext.getViewport().getScreenArea();
		ReferencedEnvelope metaArea = new ReferencedEnvelope(areaOfInterest);
		metaArea.expandBy(bufferInPixels / tileInpix.getWidth() * areaOfInterest.getWidth(),
				bufferInPixels / tileInpix.getHeight() * areaOfInterest.getHeight());
		// fetch features in meta area
		Crs layerCrs = vectorLayerService.getCrs(layer);
		Envelope layerBounds = geoService.transform(metaArea, (Crs) areaOfInterest.getCoordinateReferenceSystem(),
				layerCrs);
		Filter filter = filterService.createBboxFilter(layerCrs, layerBounds,
				layer.getLayerInfo().getFeatureInfo().getGeometryType().getName());
		if (extraInfo.getFilter() != null) {
			filter = filterService.createAndFilter(filter, filterService.parseFilter(extraInfo.getFilter()));
		}
		List<InternalFeature> features = vectorLayerService.getFeatures(vectorInfo.getServerLayerId(),
				mapContext.getCoordinateReferenceSystem(), filter, extraInfo.getStyle(),
				VectorLayerService.FEATURE_INCLUDE_ALL);

		FeatureLayer featureLayer = new FeatureLayer(createCollection(features, layer,
				mapContext.getCoordinateReferenceSystem(), style), style);
		featureLayer.setTitle(vectorInfo.getLabel());
		featureLayer.getUserData().put(USERDATA_KEY_SHOWING, extraInfo.isShowing());
		List<Rule> rules = new ArrayList<Rule>();
		double scaleDenominator = RendererUtilities.calculateOGCScale(mapContext.getAreaOfInterest(), (int) mapContext
				.getViewport().getScreenArea().getWidth(), null);
		// find the applicable rules
		for (FeatureTypeStyle fts : style.featureTypeStyles()) {
			for (Rule rule : fts.rules()) {
				if (isWithInScale(rule, scaleDenominator)) {
					FeatureIterator<SimpleFeature> it;
					try {
						it = featureLayer.getSimpleFeatureSource().getFeatures().features();
						while (it.hasNext()) {
							SimpleFeature feature = it.next();
							if (rule.isElseFilter() || rule.getFilter() == null) {
								rules.add(rule);
								break;
							} else if (rule.getFilter().evaluate(feature)) {
								rules.add(rule);
								break;
							}
						}
					} catch (IOException e) {
						// cannot happen !
					}
				}
			}
		}
		featureLayer.getUserData().put(USERDATA_KEY_STYLE_RULES, rules);
		return featureLayer;
	}

	/**
	 * Should be the same as org.geotools.renderer.lite.StreamingRenderer !
	 * 
	 * @param r
	 * @param scaleDenominator
	 * @return true if within scale
	 */
	private boolean isWithInScale(Rule r, double scaleDenominator) {
		return ((r.getMinScaleDenominator() - TOLERANCE) <= scaleDenominator)
				&& ((r.getMaxScaleDenominator() + TOLERANCE) > scaleDenominator);
	}

	private FeatureCollection<SimpleFeatureType, SimpleFeature> createCollection(List<InternalFeature> features,
			VectorLayer layer, CoordinateReferenceSystem mapCrs, Style style) {
		SimpleFeatureType type = createFeatureType(layer, mapCrs);
		ListFeatureCollection result = new ListFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
		StyleAttributeExtractor extractor = new StyleAttributeExtractor();
		style.accept(extractor);
		Set<String> styleAttributeNames = extractor.getAttributeNameSet();
		FeatureInfo featureInfo = layer.getLayerInfo().getFeatureInfo();
		for (InternalFeature internalFeature : features) {
			// 2 more attributes : geometry + style attribute
			Object[] values = new Object[internalFeature.getAttributes().size() + 2];
			int i = 0;
			for (AttributeInfo attrInfo : featureInfo.getAttributes()) {
				String name = attrInfo.getName();
				if (styleAttributeNames.contains(name)) {
					values[i++] = internalFeature.getAttributes().get(name).getValue();
				} else {
					values[i++] = null;
				}
			}
			values[i++] = internalFeature.getStyleInfo().getIndex();
			values[i++] = internalFeature.getGeometry();
			result.add(builder.buildFeature(internalFeature.getId(), values));
		}
		return result;
	}

	private SimpleFeatureType createFeatureType(VectorLayer layer, CoordinateReferenceSystem mapCrs) {
		SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
		VectorLayerInfo info = layer.getLayerInfo();
		builder.setName(info.getFeatureInfo().getDataSourceName());
		builder.setCRS(mapCrs);
		for (AttributeInfo attrInfo : info.getFeatureInfo().getAttributes()) {
			if (attrInfo instanceof PrimitiveAttributeInfo) {
				PrimitiveAttributeInfo prim = (PrimitiveAttributeInfo) attrInfo;
				switch (prim.getType()) {
					case BOOLEAN:
						builder.add(prim.getName(), Boolean.class);
						break;
					case CURRENCY:
						builder.add(prim.getName(), BigDecimal.class);
						break;
					case DATE:
						builder.add(prim.getName(), Date.class);
						break;
					case DOUBLE:
						builder.add(prim.getName(), Double.class);
						break;
					case FLOAT:
						builder.add(prim.getName(), Float.class);
						break;
					case INTEGER:
						builder.add(prim.getName(), Integer.class);
						break;
					case LONG:
						builder.add(prim.getName(), Long.class);
						break;
					case SHORT:
						builder.add(prim.getName(), Short.class);
						break;
					case STRING:
					case URL:
					case IMGURL:
						builder.add(prim.getName(), String.class);
						break;
					default:
						throw new IllegalStateException("Unknown primitive attribute type " + prim.getType());
				}
			} else if (attrInfo instanceof AssociationAttributeInfo) {
				AssociationAttributeInfo ass = (AssociationAttributeInfo) attrInfo;
				switch (ass.getType()) {
					case MANY_TO_ONE:
						builder.add(ass.getName(), Object.class);
						break;
					case ONE_TO_MANY:
						builder.add(ass.getName(), Collection.class);
						break;
					default:
						throw new IllegalStateException("Unknown association attribute type " + ass.getType());
				}
			}
		}
		// add the extra style index attribute
		builder.add(STYLE_INDEX_ATTRIBUTE_NAME, Integer.class);
		// add the geometry attribute
		GeometryAttributeInfo geom = info.getFeatureInfo().getGeometryType();
		builder.add(geom.getName(), dtoConverterService.toInternal(info.getLayerType()), mapCrs);
		builder.setDefaultGeometry(geom.getName());
		return builder.buildFeatureType();
	}

	private VectorLayerRasterizingInfo cloneInfo(VectorLayerRasterizingInfo input) throws GeomajasException {
		try {
			JBossObjectOutputStream jbossSerializer = new JBossObjectOutputStream(null);
			Object obj = jbossSerializer.smartClone(input);
			return (VectorLayerRasterizingInfo) obj;
		} catch (IOException e) {
			// should not happen
			throw new GeomajasException(e, ExceptionCode.UNEXPECTED_PROBLEM);
		}
	}

}
