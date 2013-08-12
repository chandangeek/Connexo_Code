package com.elster.jupiter.parties.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;

import java.security.Principal;

public interface ServiceLocator {

    OrmClient getOrmClient();

    Principal getPrincipal();

    ComponentCache getCache();

    Clock getClock();

    UserService getUserService();

    EventService getEventService();
}
