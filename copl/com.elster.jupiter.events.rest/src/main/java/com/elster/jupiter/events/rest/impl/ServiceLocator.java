package com.elster.jupiter.events.rest.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

public interface ServiceLocator {

    TransactionService getTransactionService();

    EventService getEventService();

    RestQueryService getRestQueryService();

}