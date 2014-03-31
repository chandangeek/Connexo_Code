package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;

/**
 * Copyrights EnergyICT
 * Date: 14/03/14
 * Time: 10:32
 */
public class PartialConnectionInitiationTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialConnectionInitiationTaskBuilder, PartialConnectionInitiationTaskImpl> implements PartialConnectionInitiationTaskBuilder {

    PartialConnectionInitiationTaskBuilderImpl(DataModel dataModel, DeviceCommunicationConfiguration configuration) {
        super(PartialConnectionInitiationTaskBuilder.class, dataModel, configuration);
    }

    @Override
    PartialConnectionInitiationTaskImpl newInstance() {
        return PartialConnectionInitiationTaskImpl.from(dataModel, configuration);
    }

}
