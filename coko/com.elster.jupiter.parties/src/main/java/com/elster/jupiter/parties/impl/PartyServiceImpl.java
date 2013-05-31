package com.elster.jupiter.parties.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Component (name = "com.elster.jupiter.parties" , service = {PartyService.class,InstallService.class}, property="name="+Bus.COMPONENTNAME)
public class PartyServiceImpl implements PartyService, InstallService , ServiceLocator {
	private volatile OrmClient ormClient; 
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile ComponentCache cache;
    private volatile Clock clock;

    private volatile QueryService queryService;

	public void activate(ComponentContext context) {
		Bus.setServiceLocator(this);
	}

	@Override
	public PartyRole createRole(String componentName, String mRID, String name, String aliasName , String description) {
		PartyRoleImpl result = new PartyRoleImpl(componentName, mRID, name, aliasName, description);
		ormClient.getPartyRoleFactory().persist(result);
		return result;
	}
	
	public void deActivate(ComponentContext context) {
		Bus.setServiceLocator(null);
	}

    @Override
	public ComponentCache getCache() {
		return cache;
	}

    public Clock getClock() {
        return clock;
    }

	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}

	@Override
	public List<Party> getParties() {
		List<PartyRepresentationImpl> representations = ormClient.getPartyRepresentationFactory().find("delegate", getPrincipal().getName());
		List<Party> result = new ArrayList<>(); 
		for (PartyRepresentationImpl representation : representations) {
			if (representation.isCurrent()) {
				result.add(representation.getParty());
			}
		}
		return result;
	}

	@Override
	public Party getParty(String mRID) {
		return ormClient.getPartyFactory().getUnique("mRID", mRID);
	}

    @Override
    public Query<Party> getPartyQuery() {
        return getQueryService().wrap(getOrmClient().getPartyFactory().with());
    }

    @Override
	public Principal getPrincipal() {
		return threadPrincipalService.getPrincipal();
	}

    public QueryService getQueryService() {
        return queryService;
    }

	@Override
	public void install() {
		new InstallerImpl().install(true,true,true);
	}

    @Override
    public Person newPerson(String firstName, String lastName) {
        return new PersonImpl(firstName, lastName);
    }
	
	@Reference(name = "ZCacheService")
	public void setCacheService (CacheService cacheService) {
		this.cache = cacheService.getComponentCache(ormClient.getDataModel());
	}

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
	
	@Reference
	public void setOrmService(OrmService ormService) {
		DataModel dataModel = ormService.getDataModel(Bus.COMPONENTNAME);
    	if (dataModel == null) {
    		dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Party Management");
    		for (TableSpecs spec : TableSpecs.values()) {
    			spec.addTo(dataModel);			
    		}	
    	}
    	ormClient = new OrmClientImpl(dataModel);    		
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }
	
	@Reference
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;
	}

    @Override
    public Party findParty(long id) {
        return getOrmClient().getPartyFactory().get(id);
    }
}
