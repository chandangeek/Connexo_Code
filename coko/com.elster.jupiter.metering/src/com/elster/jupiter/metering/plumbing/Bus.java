package com.elster.jupiter.metering.plumbing;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.parties.PartyService;

public enum Bus {
    ;
	
	public static final String COMPONENTNAME = "MTR";
	
	private static volatile ServiceLocator locator;
	
	public static void setServiceLocator(ServiceLocator locator) {
		Bus.locator = locator;
	}
	
	public static OrmClient getOrmClient() {
		return locator.getOrmClient();
	}	

	public static IdsService getIdsService() {
		return locator.getIdsService();
	}
	
	public static QueryService getQueryService() {
		return locator.getQueryService();
	}
	
	static ComponentCache getComponentCache() {
		return locator.getComponentCache();		
	}
	
	public static PartyService getPartyService() {
		return locator.getPartyService();
	}

}
