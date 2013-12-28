package com.elster.jupiter.parties.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

import java.security.Principal;
import java.util.List;

@Component(name = "com.elster.jupiter.parties", service = {PartyService.class, InstallService.class}, property = "name=" + PartyService.COMPONENTNAME)
public class PartyServiceImpl implements PartyService, InstallService {
	
	private volatile DataModel dataModel;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile QueryService queryService;
    private volatile EventService eventService;
    private volatile OrmService ormService;
    
    public PartyServiceImpl() {
    }

    @Inject
    public PartyServiceImpl(Clock clock, OrmService ormService, QueryService queryService, UserService userService, EventService eventService, ThreadPrincipalService threadPrincipalService) {
        setClock(clock);
        setOrmService(ormService);
        setQueryService(queryService);
        setUserService(userService);
        setEventService(eventService);
        setThreadPrincipalService(threadPrincipalService);
        activate();
        if (!dataModel.isInstalled()) {
        	install();
        }
    }

    Module getModule() {
    	return new AbstractModule() {	
			@Override
			public void configure() {
				bind(DataModel.class).toInstance(dataModel);
				bind(EventService.class).toInstance(eventService);
				bind(Clock.class).toInstance(clock);
				bind(UserService.class).toInstance(userService);
			}
		}; 	
    }
    
    @Activate
    public void activate() {
        dataModel.register(getModule());
    }

	@Override
	public PartyRole createRole(String componentName, String mRID, String name, String aliasName , String description) {
		PartyRoleImpl result = PartyRoleImpl.from(dataModel, componentName, mRID, name, aliasName, description);
		dataModel.persist(result);
		return result;
	}

	void clearRoleCache() {
		ormService.invalidateCache(COMPONENTNAME, TableSpecs.PRT_PARTYROLE.name());
	}
	
	@Override
	public Optional<PartyRole> getRole(String mRID) {
		return dataModel.mapper(PartyRole.class).getOptional(mRID);
	}

    @Override
    public void deletePartyRole(PartyRole partyRole) {
        dataModel.remove(partyRole);
    }

    @Override
    public Optional<Party> findParty(long id) {
        return dataModel.mapper(Party.class).getOptional(id);
    }

    public Optional<PartyRole> findPartyRoleByMRID(String mRID) {
        for (PartyRole partyRole : getPartyRoles()) {
            if (partyRole.getMRID().equals(mRID)) {
                return Optional.of(partyRole);
            }
        }
        return Optional.absent();
    }

    @Override
    public List<Party> getParties() {
        List<PartyRepresentation> representations = dataModel.mapper(PartyRepresentation.class).find("delegate", getPrincipal().getName());
        ImmutableList.Builder<Party> builder = ImmutableList.builder();
        for (PartyRepresentation representation : representations) {
            if (representation.isCurrent()) {
                builder.add(representation.getParty());
            }
        }
        return builder.build();
    }

    
    @Override 
    public Optional<Party> getParty(long id) {
    	return dataModel.mapper(Party.class).getEager(id);
    }
    
    @Override
    public Optional<Party> getParty(String mRID) {
        return dataModel.mapper(Party.class).getUnique("mRID", mRID);
    }

    @Override
    public Query<Party> getPartyQuery() {
    	return queryService.wrap(dataModel.query(Party.class,PartyInRole.class));
    }

    @Override
    public List<PartyRole> getPartyRoles() {
        return dataModel.mapper(PartyRole.class).find();
    }

    private Principal getPrincipal() {
        return threadPrincipalService.getPrincipal();
    }

    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public void install() {
        new Installer(dataModel,eventService).install(true, true, true);
    }

    @Override
    public Organization newOrganization(String mRID) {
    	return OrganizationImpl.from(dataModel, mRID);
    }

    @Override
    public Person newPerson(String firstName, String lastName) {
    	return PersonImpl.from(dataModel,firstName,lastName);
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    	dataModel = ormService.newDataModel(COMPONENTNAME, "Party Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void updateRepresentation(PartyRepresentation representation) {
        dataModel.update(representation);
    }

    @Override
    public void updateRole(PartyRole partyRole) {
        dataModel.update(partyRole);
    }

    DataModel getDataModel() {
    	return dataModel;
    }
}
