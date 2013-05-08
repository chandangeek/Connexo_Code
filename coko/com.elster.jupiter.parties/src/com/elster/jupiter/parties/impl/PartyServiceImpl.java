package com.elster.jupiter.parties.impl;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;


import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;

@Component (name = "com.elster.jupiter.parties" , service = {PartyService.class,InstallService.class}, property="name="+Bus.COMPONENTNAME)
public class PartyServiceImpl implements PartyService, InstallService , ServiceLocator {
	
	private volatile OrmClient ormClient; 
	private volatile ThreadPrincipalService threadPrincipalService;
	private volatile ComponentCache cache;

	@Override
	public Party getParty(String mRID) {
		return ormClient.getPartyFactory().getUnique("mRID", mRID);
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
	public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
		this.threadPrincipalService = threadPrincipalService;
	}
	
	@Reference(name = "ZCacheService")
	public void setCacheService (CacheService cacheService) {
		this.cache = cacheService.getComponentCache(ormClient.getDataModel());
	}

	@Activate
	public void activate() {
		Bus.setServiceLocator(this);
	}
	
	@Deactivate
	public void deActivate() {
		Bus.setServiceLocator(null);
	}
	@Override
	public OrmClient getOrmClient() {
		return ormClient;
	}
	
	@Override
	public Principal getPrincipal() {
		return threadPrincipalService.getPrincipal();
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
	public PartyRole createRole(String componentName, String mRID, String name, String aliasName , String description) {
		PartyRoleImpl result = new PartyRoleImpl(componentName, mRID, name, aliasName, description);
		ormClient.getPartyRoleFactory().persist(result);
		return result;
	}

	@Override
	public ComponentCache getCache() {
		return cache;
	}

	@Override
	public void install() {
		new InstallerImpl().install(true,true,true);
		
	}
}
