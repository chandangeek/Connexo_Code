/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.scheduling.SchedulingService;

class PartialConnectionInitiationTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialConnectionInitiationTaskBuilder, PartialConnectionInitiationTaskImpl> implements PartialConnectionInitiationTaskBuilder {

    PartialConnectionInitiationTaskBuilderImpl(DataModel dataModel, DeviceConfigurationImpl configuration, SchedulingService schedulingService, EventService eventService) {
        super(PartialConnectionInitiationTaskBuilder.class, dataModel, configuration, schedulingService, eventService);
    }

    @Override
    PartialConnectionInitiationTaskImpl newInstance() {
        return PartialConnectionInitiationTaskImpl.from(dataModel, configuration);
    }

}