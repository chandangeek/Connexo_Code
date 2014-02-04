package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EnumeratedUsagePointGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.groups.QueryUsagePointGroup;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

@Component(name = "com.elster.jupiter.metering", service = {MeteringService.class, InstallService.class}, property = "name=" + MeteringService.COMPONENTNAME)
public class MeteringGroupsServiceImpl implements MeteringGroupsService, InstallService {

    private volatile DataModel dataModel;
    private volatile MeteringService meteringService;

    public MeteringGroupsServiceImpl() {
    }

    @Inject
    public MeteringGroupsServiceImpl(OrmService ormService) {
        setOrmService(ormService);
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
        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MeteringGroupsService.class).toInstance(MeteringGroupsServiceImpl.this);
                bind(DataModel.class).toInstance(dataModel);
            }
        });
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
}
