package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

public interface ServiceLocator {

    TransactionService getTransactionService();

    EstimationService getEstimationService();
    
    RestQueryService getRestQueryService();

    MeteringService getMeteringService();
}