package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;

/**
 * Copyrights EnergyICT
 * Date: 14/03/14
 * Time: 10:32
 */
public class PartialConnectionInitiationTaskBuilderImpl extends AbstractScheduledPartialConnectionTaskBuilder<PartialConnectionInitiationTaskBuilder, PartialConnectionInitiationTask> implements PartialConnectionInitiationTaskBuilder {

    PartialConnectionInitiationTaskBuilderImpl(DataModel dataModel) {
        super(PartialConnectionInitiationTaskBuilder.class, dataModel);
    }

    @Override
    PartialConnectionInitiationTask newInstance() {
        return PartialConnectionInitiationTaskImpl.from(dataModel);
    }

}
