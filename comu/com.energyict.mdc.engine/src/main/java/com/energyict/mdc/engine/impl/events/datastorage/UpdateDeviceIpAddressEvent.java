package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Copyrights EnergyICT
 * Date: 18/02/2016
 * Time: 13:54
 */
public class UpdateDeviceIpAddressEvent extends AbstractCollectedDataProcessingEventImpl  {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.updateDeviceIpAddress.description";

    private DeviceIdentifier deviceIdentifier;

    public UpdateDeviceIpAddressEvent(ServiceProvider serviceProvider, DeviceIdentifier deviceIdentifier) {
        super(serviceProvider);
        if (deviceIdentifier == null){
            throw new IllegalArgumentException("Device identifier cannot be null");
        }
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("updateDeviceIpAddress");
        writer.object();
        writer.key("deviceIdentifier").value(this.deviceIdentifier.toString());
        writer.endObject();
    }
}
