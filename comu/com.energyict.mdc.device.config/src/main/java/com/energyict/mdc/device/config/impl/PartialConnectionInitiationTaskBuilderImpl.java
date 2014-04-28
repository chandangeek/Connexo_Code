package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.scheduling.SchedulingService;

/**
 * Copyrights EnergyICT
 * Date: 14/03/14
 * Time: 10:32
 */
public class PartialConnectionInitiationTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialConnectionInitiationTaskBuilder, PartialConnectionInitiationTaskImpl> implements PartialConnectionInitiationTaskBuilder {

    PartialConnectionInitiationTaskBuilderImpl(DataModel dataModel, DeviceCommunicationConfiguration configuration, SchedulingService schedulingService) {
        super(PartialConnectionInitiationTaskBuilder.class, dataModel, configuration, schedulingService);
    }

    @Override
    PartialConnectionInitiationTaskImpl newInstance() {
        return PartialConnectionInitiationTaskImpl.from(dataModel, configuration);
    }

}
