package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.energyict.mdc.scheduling", service = SchedulingService.class, immediate = true)
public class SchedulingServiceImpl implements SchedulingService {

    private volatile OrmService ormService;

    public SchedulingServiceImpl() {
    }

    @Inject
    public SchedulingServiceImpl(OrmService ormService) {
        setOrmService(ormService);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
        DataModel dataModel = ormService.newDataModel(SchedulingService.COMPONENT_NAME, "Scheduling service");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }



    @Override
    public NextExecutionSpecs findNextExecutionSpecs(long id) {
        return null;
    }

    @Override
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression) {
        return null;
    }
}
