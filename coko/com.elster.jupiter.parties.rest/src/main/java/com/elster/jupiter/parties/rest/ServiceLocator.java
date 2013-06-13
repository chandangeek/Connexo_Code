package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;

interface ServiceLocator {

    TransactionService getTransactionService();

    RestQueryService getQueryService();

    PartyService getPartyService();

    UserService getUserService();

    Clock getClock();
}
