package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

interface ServiceLocator {

    TransactionService getTransactionService();

    RestQueryService getQueryService();

    PartyService getPartyService();
}
