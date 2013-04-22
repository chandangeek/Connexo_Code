package com.elster.jupiter.metering.plumbing;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.orm.cache.CacheService;

interface ServiceLocator {
	OrmClient getOrmClient();
	CacheService getCacheService();
	IdsService getIdsService();
	QueryService getQueryService();
	
}

