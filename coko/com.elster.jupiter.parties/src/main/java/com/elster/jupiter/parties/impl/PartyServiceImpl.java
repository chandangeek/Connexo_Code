package com.elster.jupiter.parties.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.parties.Organization;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.Person;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.Principal;
import java.util.List;

@Component(name = "com.elster.jupiter.parties", service = {PartyService.class, InstallService.class}, property = "name=" + Bus.COMPONENTNAME)
public class PartyServiceImpl implements PartyService, InstallService, ServiceLocator {
    private volatile OrmClient ormClient;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile ComponentCache cache;
    private volatile Clock clock;
    private volatile UserService userService;
    private volatile QueryService queryService;
    private volatile EventService eventService;

    public PartyServiceImpl() {
    }

    @Inject
    public PartyServiceImpl(Clock clock, OrmService ormService, QueryService queryService, UserService userService, CacheService cacheService, EventService eventService, ThreadPrincipalService threadPrincipalService) {
        this.clock = clock;
        initOrmClient(ormService);
        this.queryService = queryService;
        this.userService = userService;
        initComponentCache(cacheService);
        this.eventService = eventService;
        this.threadPrincipalService = threadPrincipalService;
        activate();
        install();
    }

    @Activate
    public void activate() {
        Bus.setServiceLocator(this);
    }

	@Override
	public PartyRole createRole(String componentName, String mRID, String name, String aliasName , String description) {
		PartyRoleImpl result = new PartyRoleImpl(componentName, mRID, name, aliasName, description);
		ormClient.getPartyRoleFactory().persist(result);
		return result;
	}

    @Deactivate
	public void deactivate() {
		Bus.clearServiceLocator(this);
	}

    @Override
    public void deletePartyRole(PartyRole partyRole) {
        getOrmClient().getPartyRoleFactory().remove(partyRole);
    }

    @Override
    public Optional<Party> findParty(long id) {
        return getOrmClient().getPartyFactory().get(id);
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
    public ComponentCache getCache() {
        return cache;
    }

    public Clock getClock() {
        return clock;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Override
    public OrmClient getOrmClient() {
        return ormClient;
    }

    @Override
    public List<Party> getParties() {
        List<PartyRepresentation> representations = ormClient.getPartyRepresentationFactory().find("delegate", getPrincipal().getName());
        ImmutableList.Builder<Party> builder = ImmutableList.builder();
        for (PartyRepresentation representation : representations) {
            if (representation.isCurrent()) {
                builder.add(representation.getParty());
            }
        }
        return builder.build();
    }

    @Override
    public Optional<Party> getParty(String mRID) {
        return ormClient.getPartyFactory().getUnique("mRID", mRID);
    }

    @Override
    public Query<Party> getPartyQuery() {
        return getQueryService().wrap(getOrmClient().getPartyFactory().with());
    }

    @Override
    public List<PartyRole> getPartyRoles() {
        return getOrmClient().getPartyRoleFactory().find();
    }

    @Override
    public Principal getPrincipal() {
        return threadPrincipalService.getPrincipal();
    }

    public QueryService getQueryService() {
        return queryService;
    }

    @Override
    public UserService getUserService() {
        return userService;
    }

    @Override
    public void install() {
        new InstallerImpl().install(true, true, true);
    }

    @Override
    public Organization newOrganization(String mRID) {
        return new OrganizationImpl(mRID);
    }

    @Override
    public Person newPerson(String firstName, String lastName) {
        return new PersonImpl(firstName, lastName);
    }

    @Reference(name = "ZCacheService")
    public void setCacheService(CacheService cacheService) {
        initComponentCache(cacheService);
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
        initOrmClient(ormService);
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
        getOrmClient().getPartyRepresentationFactory().update(representation);
    }

    @Override
    public void updateRole(PartyRole partyRole) {
        getOrmClient().getPartyRoleFactory().update(partyRole);
    }

    private void initComponentCache(CacheService cacheService) {
        this.cache = cacheService.createComponentCache(ormClient.getDataModel());
    }

    private void initOrmClient(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(Bus.COMPONENTNAME, "Party Management");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
        ormClient = new OrmClientImpl(dataModel);
    }
}
