package com.elster.jupiter.metering.plumbing;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.orm.cache.ComponentCache;

public interface ServiceLocator {
	OrmClient getOrmClient();
	ComponentCache getComponentCache();
	IdsService getIdsService();
	QueryService getQueryService();
	
}

