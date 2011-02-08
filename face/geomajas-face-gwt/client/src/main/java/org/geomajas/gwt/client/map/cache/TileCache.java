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

package org.geomajas.gwt.client.map.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geomajas.gwt.client.map.MapViewState;
import org.geomajas.gwt.client.map.cache.tile.TileFunction;
import org.geomajas.gwt.client.map.cache.tile.VectorTile;
import org.geomajas.gwt.client.map.feature.Feature;
import org.geomajas.gwt.client.map.feature.LazyLoadCallback;
import org.geomajas.gwt.client.map.feature.LazyLoader;
import org.geomajas.gwt.client.map.layer.VectorLayer;
import org.geomajas.gwt.client.spatial.Bbox;
import org.geomajas.layer.tile.TileCode;

/**
 * Cache for tiles and contained features.
 * 
 * @author Pieter De Graef
 * @author Jan De Moerloose
 */
public class TileCache implements SpatialCache {

	protected VectorLayer layer;

	protected Bbox layerBounds;

	protected Map<String, VectorTile> tiles;

	protected Map<String, Feature> features;

	private int currentTileLevel;

	private int currentMinX;

	private int currentMinY;

	private int currentMaxX;

	private int currentMaxY;

	private List<VectorTile> evictedTiles;

	private MapViewState lastViewState;

	private String cacheFilter; // filter which was used when filling the cache

	// -------------------------------------------------------------------------
	// Constructors:
	// -------------------------------------------------------------------------

	/**
	 * The default <code>SpatialCache</code> implementation. Caches <code>VectorTile</code>s, based on bounds.
	 * 
	 * @param layer
	 *            Reference to the <code>VectorLayer</code> object.
	 * @param layerBounds
	 *            The maximum bounds for this cache.
	 */
	public TileCache(VectorLayer layer, Bbox layerBounds) {
		this.layer = layer;
		this.layerBounds = layerBounds;
		tiles = new HashMap<String, VectorTile>();
		features = new HashMap<String, Feature>();
		evictedTiles = new ArrayList<VectorTile>();
		currentMinX = 0;
		currentMinY = 0;
		currentMaxX = 0;
		currentMaxY = 0;
	}

	// -------------------------------------------------------------------------
	// SpatialCache implementation:
	// -------------------------------------------------------------------------

	/**
	 * Adds the tile with the specified code to the cache or simply returns the tile if it's already in the cache.
	 * 
	 * @param tileCode
	 *            A {@link TileCode} instance.
	 */
	public VectorTile addTile(TileCode tileCode) {
		VectorTile tile = tiles.get(tileCode.toString());
		if (tile == null) {
			tile = new VectorTile(tileCode, calcBoundsForTileCode(tileCode), this);
			tiles.put(tileCode.toString(), tile);
		}
		return tile;
	}

	public void getFeaturesByCode(TileCode code, int featureIncludes, LazyLoadCallback callback) {
		if (code != null) {
			VectorTile tile = tiles.get(code.toString());
			if (tile != null) {
				tile.getFeatures(featureIncludes, callback);
			}
		}
	}

	public VectorLayer getLayer() {
		return layer;
	}

	// -------------------------------------------------------------------------
	// VectorLayerStore implementation:
	// -------------------------------------------------------------------------

	public boolean contains(String id) {
		return features.containsKey(id);
	}

	public void getFeature(String id, int featureIncludes, LazyLoadCallback callback) {
		List<Feature> list = new ArrayList<Feature>();
		list.add(features.get(id));
		LazyLoader.lazyLoad(list, featureIncludes, callback);
	}

	public Feature getPartialFeature(String id) {
		return features.get(id);
	}

	public void getFeatures(int featureIncludes, LazyLoadCallback callback) {
		List<Feature> list = new ArrayList<Feature>(features.values());
		LazyLoader.lazyLoad(list, featureIncludes, callback);
	}

	public boolean addFeature(Feature feature) {
		if (!features.containsKey(feature.getId())) {
			features.put(feature.getId(), feature);
			return true;
		} else {
			Feature localFeature = features.get(feature.getId());
			if (feature.isAttributesLoaded()) {
				localFeature.setAttributes(feature.getAttributes());
			}
			if (feature.isGeometryLoaded()) {
				localFeature.setGeometry(feature.getGeometry());
			}
			localFeature.setDeletable(feature.isDeletable());
			localFeature.setUpdatable(feature.isUpdatable());
			localFeature.setStyleId(feature.getStyleId());
			return false;
		}
	}

	public Feature removeFeature(String id) {
		return features.remove(id);
	}

	public int size() {
		return features.size();
	}

	public Feature newFeature() {
		return new Feature(getLayer());
	}

	public void query(Bbox bbox, TileFunction<VectorTile> callback) {
		List<VectorTile> setTiles = new ArrayList<VectorTile>();

		// Create the full list of tile on which to operate:
		List<TileCode> tileCodes = calcCodesForBounds(bbox);
		for (TileCode tileCode : tileCodes) {
			if (tiles.containsKey(tileCode.toString())) {
				VectorTile tile = tiles.get(tileCode.toString());
				if (!setTiles.contains(tile)) {
					setTiles.add(tile);
				}
				// Also process tiles of features that partly lie in this tile but were assigned to another tile.
				List<TileCode> codesExtraFeatures = tile.getCodes();
				for (TileCode extraCode : codesExtraFeatures) {
					VectorTile extraTile = tiles.get(extraCode.toString());
					if (!setTiles.contains(extraTile)) {
						setTiles.add(extraTile);
					}
				}
			}
		}

		// Now execute the SpatialNodeFunction function on the entire list:
		for (VectorTile tile : setTiles) {
			callback.execute(tile);
		}
	}

	public void queryAndSync(Bbox bbox, String filter, TileFunction<VectorTile> onDelete,
			TileFunction<VectorTile> onUpdate) {
		MapViewState viewState = layer.getMapModel().getMapView().getViewState();
		boolean panning = lastViewState == null || viewState.isPannableFrom(lastViewState);
		if (!panning || isDirty(filter)) {
			// Delete all tiles
			clear();
			for (VectorTile tile : evictedTiles) {
				tile.cancel();
				if (onDelete != null) {
					onDelete.execute(tile);
				}
			}
			evictedTiles.clear();
		}
		lastViewState = layer.getMapModel().getMapView().getViewState();

		// Only fetch when inside the layer bounds:
		if (bbox.intersects(layerBounds)) {
			// Check tile level:
			int bestLevel = calculateTileLevel(bbox);
			if (bestLevel != currentTileLevel) {
				currentTileLevel = bestLevel;
			}

			// Find needed tile codes:
			List<TileCode> tileCodes = calcCodesForBounds(bbox);

			// Make a clone, as we are going to modify the actual node map:
			Map<String, VectorTile> currentNodes = new HashMap<String, VectorTile>(tiles);
			for (TileCode tileCode : tileCodes) {
				VectorTile tile = currentNodes.get(tileCode.toString());
				if (null == tile) {
					tile = addTile(tileCode); // Add the node
				}
				tile.apply(filter, onUpdate);
				tile.applyConnected(filter, onUpdate);
			}
		}
	}

	// -------------------------------------------------------------------------
	// LayerStore implementation:
	// -------------------------------------------------------------------------

	public void clear() {
		// clear the tiles, the features should not be dropped as they are reloaded anyhow
		evictedTiles.addAll(tiles.values());
		tiles.clear();
	}

	public boolean isDirty() {
		return isDirty(cacheFilter);
	}
	
	public boolean isDirty(String filter) {
		return !evictedTiles.isEmpty() || !objectEquals(filter, cacheFilter);
	}

	private boolean objectEquals(Object left, Object right) {
		if (null == left) {
			return null == right;
		} else {
			return left.equals(right);
		}
	}

	public Collection<VectorTile> getTiles() {
		return tiles.values();
	}
	
	public Bbox getLayerBounds() {
		return layerBounds;
	}


	// -------------------------------------------------------------------------
	// Private functions:
	// -------------------------------------------------------------------------

	/**
	 * Calculate the best tile level to use for a certain view-bounds.
	 * 
	 * @param bounds
	 *            view bounds
	 * @return best tile level for view bounds
	 */
	protected int calculateTileLevel(Bbox bounds) {
		double baseX = layerBounds.getWidth();
		double baseY = layerBounds.getHeight();
		// choose the tile level so the area is between 256*256 and 512*512 pixels
		double baseArea = baseX * baseY;
		double scale = layer.getMapModel().getMapView().getCurrentScale();
		double osmArea = 256 * 256 / (scale * scale);
		int tileLevel = (int) Math.floor(Math.log(baseArea / osmArea) / Math.log(4.0));
		if (tileLevel < 0) {
			tileLevel = 0;
		}
		return tileLevel;
	}

	/**
	 * Calculate the exact bounding box for a tile, given it's tile-code.
	 * 
	 * @param tileCode
	 *            tile code
	 * @return bbox for tile
	 */
	protected Bbox calcBoundsForTileCode(TileCode tileCode) {
		// Calculate tile width and height for tileLevel=tileCode.getTileLevel()
		double div = Math.pow(2, tileCode.getTileLevel());
		double scale = layer.getMapModel().getMapView().getCurrentScale();
		double tileWidth = Math.ceil((scale * layerBounds.getWidth()) / div) / scale;
		double tileHeight = Math.ceil((scale * layerBounds.getHeight()) / div) / scale;

		// Now calculate indexes, and return bbox:
		double x = layerBounds.getX() + tileCode.getX() * tileWidth;
		double y = layerBounds.getY() + tileCode.getY() * tileHeight;
		return new Bbox(x, y, tileWidth, tileHeight);
	}

	/**
	 * Saves the complete array of TileCode objects for the given bounds (and the current scale).
	 * 
	 * @param bounds
	 *            view bounds
	 * @return list of tiles in these bounds
	 */
	protected List<TileCode> calcCodesForBounds(Bbox bounds) {
		// Calculate tile width and height for tileLevel=currentTileLevel
		double div = Math.pow(2, currentTileLevel); // tile level must be correct!
		double scale = layer.getMapModel().getMapView().getCurrentScale();
		double tileWidth = Math.ceil((scale * layerBounds.getWidth()) / div) / scale;
		double tileHeight = Math.ceil((scale * layerBounds.getHeight()) / div) / scale;

		// For safety (to prevent division by 0):
		List<TileCode> codes = new ArrayList<TileCode>();
		if (tileWidth == 0 || tileHeight == 0) {
			return codes;
		}

		// Calculate bounds relative to extents:
		Bbox clippedBounds = bounds.intersection(layerBounds);
		if (clippedBounds == null) {
			// TODO throw error? If this is null, then the server configuration is incorrect.
			return codes;
		}
		double relativeBoundX = Math.abs(clippedBounds.getX() - layerBounds.getX());
		double relativeBoundY = Math.abs(clippedBounds.getY() - layerBounds.getY());
		currentMinX = (int) Math.floor(relativeBoundX / tileWidth);
		currentMinY = (int) Math.floor(relativeBoundY / tileHeight);
		currentMaxX = (int) Math.ceil((relativeBoundX + clippedBounds.getWidth()) / tileWidth) - 1;
		currentMaxY = (int) Math.ceil((relativeBoundY + clippedBounds.getHeight()) / tileHeight) - 1;

		// Now fill the list with the correct codes:
		for (int x = currentMinX; x <= currentMaxX; x++) {
			for (int y = currentMinY; y <= currentMaxY; y++) {
				codes.add(new TileCode(currentTileLevel, x, y));
			}
		}
		return codes;
	}
}
