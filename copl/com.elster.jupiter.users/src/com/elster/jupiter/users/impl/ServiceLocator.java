package com.elster.jupiter.users.impl;

import com.elster.jupiter.transaction.TransactionService;

interface ServiceLocator {
	OrmClient getOrmClient(); 	
	TransactionService getTransactionService();
}
