package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

@Component(name = "com.elster.jupiter.metering", service = {MeteringGroupsService.class, InstallService.class}, property = "name=" + MeteringGroupsService.COMPONENTNAME, immediate = true)
public class MeteringGroupsServiceImpl implements MeteringGroupsService, InstallService {

    private volatile DataModel dataModel;
    private volatile MeteringService meteringService;
    private volatile QueryService queryService;

    private volatile List<EndDeviceQueryProvider> endDeviceQueryProviders = new ArrayList<EndDeviceQueryProvider>();

    public MeteringGroupsServiceImpl() {
    }

    @Inject
    public MeteringGroupsServiceImpl(OrmService ormService, MeteringService meteringService, QueryService queryService) {
        setOrmService(ormService);
        setMeteringService(meteringService);
        setQueryService(queryService);
        activate();
        if (!dataModel.isInstalled()) {
        	install();
        }
    }

    @Override
    public void install() {
    	dataModel.install(true, true);
    }

    @Activate
    public void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(MeteringGroupsService.class).toInstance(MeteringGroupsServiceImpl.this);
                    bind(MeteringService.class).toInstance(meteringService);
                    bind(DataModel.class).toInstance(dataModel);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public QueryUsagePointGroup createQueryUsagePointGroup(Condition condition) {
        QueryUsagePointGroupImpl queryUsagePointGroup = new QueryUsagePointGroupImpl(dataModel, meteringService);
        queryUsagePointGroup.setCondition(condition);
        return queryUsagePointGroup;
    }

    @Override
    public Optional<QueryUsagePointGroup> findQueryUsagePointGroup(long id) {
        return dataModel.mapper(QueryUsagePointGroup.class).getOptional(id);
    }

    @Override
    public EnumeratedUsagePointGroup createEnumeratedUsagePointGroup(String name) {
        EnumeratedUsagePointGroup group = new EnumeratedUsagePointGroupImpl(dataModel);
        group.setName(name);
        return group;
    }

    @Override
    public Optional<EnumeratedUsagePointGroup> findEnumeratedUsagePointGroup(long id) {
        return dataModel.mapper(EnumeratedUsagePointGroup.class).getOptional(id);
    }

    @Override
    public Optional<UsagePointGroup> findUsagePointGroup(String mRID) {
        List<UsagePointGroup> found = dataModel.mapper(UsagePointGroup.class).select(Operator.EQUAL.compare("mRID", mRID));
        return found.isEmpty() ? Optional.<UsagePointGroup>absent() : Optional.of(found.get(0));
    }

    @Override
    public QueryEndDeviceGroup createQueryEndDeviceGroup(Condition condition) {
        QueryEndDeviceGroupImpl queryUsagePointGroup = new QueryEndDeviceGroupImpl(dataModel, meteringService, this);
        queryUsagePointGroup.setCondition(condition);
        return queryUsagePointGroup;
    }

    @Override
    public EnumeratedEndDeviceGroup createEnumeratedEndDeviceGroup(String name) {
        EnumeratedEndDeviceGroup group = new EnumeratedEndDeviceGroupImpl(dataModel);
        group.setName(name);
        return group;
    }

    @Override
    public Optional<EnumeratedEndDeviceGroup> findEnumeratedEndDeviceGroup(long id) {
        return dataModel.mapper(EnumeratedEndDeviceGroup.class).getOptional(id);
    }

    @Override
    public Optional<QueryEndDeviceGroup> findQueryEndDeviceGroup(long id) {
        return dataModel.mapper(QueryEndDeviceGroup.class).getOptional(id);
    }

    /*@Override
    public Finder<EndDeviceGroup> findAllEndDeviceGroups() {
        return DefaultFinder.of(EndDeviceGroup.class, dataModel).defaultSortColumn("lower(name)");
    }*/

    @Override
    public Query<EndDeviceGroup> getEndDeviceGroupQuery() {
        Query<EndDeviceGroup> ruleSetQuery = queryService.wrap(dataModel.query(EndDeviceGroup.class));
        return ruleSetQuery;
    }

    @Override
    public List<EndDeviceGroup> findEndDeviceGroups() {
        return dataModel.mapper(EndDeviceGroup.class).find();
    }

    @Override
    public Optional<EndDeviceGroup> findEndDeviceGroup(String mRID) {
        List<EndDeviceGroup> found = dataModel.mapper(EndDeviceGroup.class).select(Operator.EQUAL.compare("mRID", mRID));
        return found.isEmpty() ? Optional.<EndDeviceGroup>absent() : Optional.of(found.get(0));
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(COMPONENTNAME, "CIM Metering Groups");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addEndDeviceQueryProvider(EndDeviceQueryProvider endDeviceQueryProvider) {
        endDeviceQueryProviders.add(endDeviceQueryProvider);
    }

    public void removeEndDeviceQueryProvider(EndDeviceQueryProvider endDeviceQueryProvider) {
        endDeviceQueryProviders.remove(endDeviceQueryProvider);
    }

    public EndDeviceQueryProvider getEndDeviceQueryProvider(String name) {
        for (EndDeviceQueryProvider endDeviceQueryProvider : endDeviceQueryProviders) {
            if (endDeviceQueryProvider.getName().equals(name)) {
                return endDeviceQueryProvider;
            }
        }
        return null;
    }


}
