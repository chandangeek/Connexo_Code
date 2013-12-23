package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;

public interface ServiceLocator {

    OrmClient getOrmClient();

    IdsService getIdsService();

    QueryService getQueryService();

    PartyService getPartyService();

    Clock getClock();

    UserService getUserService();

    EventService getEventService();

    ChannelBuilder getChannelBuilder();
    
    MeteringService getMeteringService();
}

