package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.device.data.rest.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

public class ConnectionTaskSuccessIndicatorAdapter extends MapBasedXmlAdapter<ConnectionTask.SuccessIndicator> {

    public ConnectionTaskSuccessIndicatorAdapter() {
        register(MessageSeeds.SUCCESS.getKey(), ConnectionTask.SuccessIndicator.SUCCESS);
        register(MessageSeeds.FAILURE.getKey(), ConnectionTask.SuccessIndicator.FAILURE);
        register(MessageSeeds.NOT_APPLICABLE.getKey(), ConnectionTask.SuccessIndicator.NOT_APPLICABLE);
    }
}
