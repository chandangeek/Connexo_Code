package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.transaction.TransactionService;

interface ServiceLocator {
	OrmClient getOrmClient();
	ComponentCache getComponentCache();
	TransactionService getTransactionService();
}
