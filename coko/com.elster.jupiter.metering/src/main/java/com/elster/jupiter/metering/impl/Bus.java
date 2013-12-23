package com.elster.jupiter.metering.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public enum Bus {
    ;
	
	public static final String COMPONENTNAME = "MTR";
	
	private static AtomicReference<ServiceLocator> locatorHolder = new AtomicReference<>();
	
	public static void setServiceLocator(ServiceLocator locator) {
		Bus.locatorHolder.set(Objects.requireNonNull(locator));
	}
	
	public static void clearServiceLocator(ServiceLocator old) {
		locatorHolder.compareAndSet(Objects.requireNonNull(old), null);
	}
	
	private static ServiceLocator getLocator() {
		return locatorHolder.get();
	}
	
	public static OrmClient getOrmClient() {
		return getLocator().getOrmClient();
	}	

	public static IdsService getIdsService() {
		return getLocator().getIdsService();
	}
	
	public static QueryService getQueryService() {
		return getLocator().getQueryService();
	}
	
	public static PartyService getPartyService() {
		return getLocator().getPartyService();
	}

    public static Clock getClock() {
        return getLocator().getClock();
    }

	public static UserService getUserService() {
		return getLocator().getUserService();
	}

    public static EventService getEventService() {
        return getLocator().getEventService();
    }

    public static ChannelBuilder getChannelBuilder() {
        return getLocator().getChannelBuilder();
    }
    
    public static MeteringService getMeteringService() {
    	return getLocator().getMeteringService();
    }
}
