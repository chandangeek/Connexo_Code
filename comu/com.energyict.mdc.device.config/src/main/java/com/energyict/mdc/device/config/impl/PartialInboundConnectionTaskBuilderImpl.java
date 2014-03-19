package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.engine.model.InboundComPortPool;

/**
 * Copyrights EnergyICT
 * Date: 14/03/14
 * Time: 10:09
 */
public class PartialInboundConnectionTaskBuilderImpl extends AbstractPartialConnectionTaskBuilder<PartialInboundConnectionTaskBuilder, InboundComPortPool, PartialInboundConnectionTask> implements PartialInboundConnectionTaskBuilder {

    PartialInboundConnectionTaskBuilderImpl(DataModel dataModel, DeviceCommunicationConfiguration configuration) {
        super(PartialInboundConnectionTaskBuilder.class, dataModel, configuration);
    }

    @Override
    void populate(PartialInboundConnectionTask instance) {
        instance.setComportPool(comPortPool);
    }

    @Override
    PartialInboundConnectionTask newInstance() {
        return PartialInboundConnectionTaskImpl.from(dataModel, configuration);
    }
}
