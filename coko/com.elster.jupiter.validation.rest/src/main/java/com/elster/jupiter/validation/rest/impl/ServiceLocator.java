package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;

public interface ServiceLocator {

    TransactionService getTransactionService();

    ValidationService getValidationService();
    
    RestQueryService getRestQueryService();

    MeteringService getMeteringService();
}