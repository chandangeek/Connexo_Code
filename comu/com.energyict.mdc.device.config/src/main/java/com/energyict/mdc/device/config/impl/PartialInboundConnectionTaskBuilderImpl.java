/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTaskBuilder;

class PartialInboundConnectionTaskBuilderImpl extends AbstractPartialConnectionTaskBuilder<PartialInboundConnectionTaskBuilder, InboundComPortPool, PartialInboundConnectionTask> implements PartialInboundConnectionTaskBuilder {

    PartialInboundConnectionTaskBuilderImpl(DataModel dataModel, DeviceConfigurationImpl configuration) {
        super(dataModel.getInstance(EventService.class), PartialInboundConnectionTaskBuilder.class, dataModel, configuration);
    }

    @Override
    void populate(PartialInboundConnectionTask instance) {
        super.populate(instance);
        instance.setComportPool(comPortPool);
        instance.setDefault(asDefault);
    }

    @Override
    public PartialInboundConnectionTaskBuilder asDefault(boolean asDefault) {
        this.asDefault = asDefault;
        return myself;
    }

    @Override
    PartialInboundConnectionTask newInstance() {
        return PartialInboundConnectionTaskImpl.from(dataModel, configuration);
    }

}
