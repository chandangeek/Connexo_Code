package com.energyict.mdc.scheduling.model.impl;

import com.energyict.mdc.scheduling.NextExecutionSpecs;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.TemporalExpression;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.energyict.mdc.scheduling", service = SchedulingService.class, immediate = true)
public class SchedulingServiceImpl implements SchedulingService {

    public SchedulingServiceImpl() {
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
