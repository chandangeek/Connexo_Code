package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.impl.commands.store.StoreConfigurationUserFile;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Copyrights EnergyICT
 * Date: 18/02/2016
 * Time: 13:54
 */
public class StoreConfigurationEvent extends AbstractCollectedDataProcessingEventImpl  {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.storeConfiguration.description";

    private DeviceIdentifier deviceIdentifier;

    public StoreConfigurationEvent(AbstractComServerEventImpl.ServiceProvider serviceProvider, DeviceIdentifier deviceIdentifier) {
        super(serviceProvider);
        if (deviceIdentifier == null){
            throw new IllegalArgumentException("DeviceIdentifier cannot be null");
        }
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("storeConfiguration");
        writer.object();
        writer.key("deviceIdentifier").value(this.deviceIdentifier.toString());
        writer.endObject();
    }
}
