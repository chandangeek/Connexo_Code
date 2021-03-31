/*
 * Copyright (c) 2021 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.CollectedCreditAmountDeviceCommand;
import com.energyict.mdc.upl.meterdata.CollectedCreditAmount;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link CollectedCreditAmountDeviceCommand}
 */
public class CollectedCreditAmountEvent extends AbstractCollectedDataProcessingEventImpl<CollectedCreditAmount> {

    public CollectedCreditAmountEvent(ServiceProvider serviceProvider, CollectedCreditAmount creditAmount) {
        super(serviceProvider, creditAmount);
    }

    @Override
    public String getDescription() {
        return CollectedCreditAmountDeviceCommand.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        CollectedCreditAmount creditAmount = getPayload();

        writer.key("collectedCreditAmount");
        writer.object();
        writer.key("deviceIdentifier").value(creditAmount.getDeviceIdentifier().toString());
        writer.key("creditAmount").value(creditAmount.getCreditAmount());
        writer.endObject();
    }
}