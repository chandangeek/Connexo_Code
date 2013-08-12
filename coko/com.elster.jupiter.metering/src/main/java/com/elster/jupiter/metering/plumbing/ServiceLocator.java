package com.elster.jupiter.metering.plumbing;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;

public interface ServiceLocator {

    OrmClient getOrmClient();

    ComponentCache getComponentCache();

    IdsService getIdsService();

    QueryService getQueryService();

    PartyService getPartyService();

    Clock getClock();

    UserService getUserService();

    EventService getEventService();
}

