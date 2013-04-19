package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.FinderService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.ids.IdsService;

interface ServiceLocator {
	OrmClient getOrmClient();
	IdsService getIdsService();
	FinderService getFinderService();
	QueryService getQueryService();
}

