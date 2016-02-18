package com.energyict.mdc.engine.impl.events.datastorage;

import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.util.Pair;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommandImpl;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Copyrights EnergyICT
 * Date: 18/02/2016
 * Time: 11:47
 */
public class NoopCollectedDataEvent extends AbstractCollectedDataProcessingEventImpl{

    private final static String DESCRIPTION = "collectedDataProcessingEvent.noop.description";

    @SuppressWarnings("unchecked")
    public NoopCollectedDataEvent(ServiceProvider serviceProvider) {
        super(serviceProvider, null);
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        // do nothing as nothing can be specified
       writer.key("meterDataStorage");
    }
}
