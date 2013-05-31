package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

public interface ServiceLocator {
	MeteringService getMeteringService();
	TransactionService getTransactionService();
	RestQueryService getQueryService();
}
