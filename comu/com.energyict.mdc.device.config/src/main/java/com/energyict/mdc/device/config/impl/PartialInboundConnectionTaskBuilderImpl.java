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
public class PartialInboundConnectionTaskBuilderImpl extends AbstractPartialConnectionTaskBuilder<PartialInboundConnectionTaskBuilder, InboundComPortPool, PartialInboundConnectionTaskImpl> implements PartialInboundConnectionTaskBuilder {

    PartialInboundConnectionTaskBuilderImpl(DataModel dataModel, DeviceCommunicationConfiguration configuration) {
        super(PartialInboundConnectionTaskBuilder.class, dataModel, configuration);
    }

    @Override
    void populate(PartialInboundConnectionTaskImpl instance) {
        instance.setComportPool(comPortPool);
        instance.setDefault(asDefault);
    }

    @Override
    public PartialInboundConnectionTaskBuilder asDefault(boolean asDefault) {
        this.asDefault = asDefault;
        return myself;
    }

    @Override
    PartialInboundConnectionTaskImpl newInstance() {
        return PartialInboundConnectionTaskImpl.from(dataModel, configuration);
    }
}
