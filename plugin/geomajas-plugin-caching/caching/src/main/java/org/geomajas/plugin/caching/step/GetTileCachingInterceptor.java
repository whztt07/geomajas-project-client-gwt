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
package org.geomajas.plugin.caching.step;

import org.geomajas.global.GeomajasException;
import org.geomajas.layer.pipeline.GetTileContainer;
import org.geomajas.layer.tile.InternalTile;
import org.geomajas.plugin.caching.service.CacheCategory;
import org.geomajas.service.TestRecorder;
import org.geomajas.service.pipeline.PipelineCode;
import org.geomajas.service.pipeline.PipelineContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Interceptor for caching the tile.
 * 
 * @author Jan De Moerloose
 * 
 */
public class GetTileCachingInterceptor extends AbstractCachingInterceptor<GetTileContainer> {

	@Autowired
	private TestRecorder recorder;

	private static final String[] KEYS = { PipelineCode.LAYER_ID_KEY, PipelineCode.TILE_METADATA_KEY,
			PipelineCode.FEATURE_INCLUDES_KEY };

	public ExecutionMode beforeSteps(PipelineContext context, GetTileContainer response) throws GeomajasException {
		TileCacheContainer cc = getContainer(CacheStepConstant.CACHE_TILE_KEY, KEYS,
				CacheCategory.TILE, context);
		if (cc != null) {
			recorder.record(CacheCategory.TILE, "Got item from cache");
			response.getTile().setFeatures(cc.getTile().getFeatures());
			response.getTile().setFeatureContent(cc.getTile().getFeatureContent());
			context.setFinished(true);
			return ExecutionMode.EXECUTE_NONE;
		}
		return ExecutionMode.EXECUTE_ALL;
	}

	public void afterSteps(PipelineContext context, GetTileContainer response) throws GeomajasException {
		recorder.record(CacheCategory.TILE, "Put item in cache");
		InternalTile tile = response.getTile();
		putContainer(context, CacheCategory.TILE, KEYS, CacheStepConstant.CACHE_TILE_KEY,
				new TileCacheContainer(tile), tile.getBounds());

	}

}
