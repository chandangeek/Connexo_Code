/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.common.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.scheduling.SchedulingService;

class PartialConnectionInitiationTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialConnectionInitiationTaskBuilder, PartialConnectionInitiationTask> implements PartialConnectionInitiationTaskBuilder {

    PartialConnectionInitiationTaskBuilderImpl(DataModel dataModel, DeviceConfigurationImpl configuration, SchedulingService schedulingService, EventService eventService) {
        super(PartialConnectionInitiationTaskBuilder.class, dataModel, configuration, schedulingService, eventService);
    }

    @Override
    PartialConnectionInitiationTaskImpl newInstance() {
        return PartialConnectionInitiationTaskImpl.from(dataModel, configuration);
    }

}