package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceProtocolProperty;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link UpdateDeviceProtocolProperty}
 */
public class UpdateDeviceProtocolPropertyEvent extends AbstractCollectedDataProcessingEventImpl {

    private final DeviceIdentifier deviceIdentifier;
    private final String propertyName;
    private final Object propertyValue;

    public UpdateDeviceProtocolPropertyEvent(ServiceProvider serviceProvider,
                                             DeviceIdentifier deviceIdentifier,
                                             String propertyName,
                                             Object propertyValue) {
        super(serviceProvider);
        this.deviceIdentifier = deviceIdentifier;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }

    @Override
    public String getDescription() {
        return UpdateDeviceProtocolProperty.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("updateDeviceProtocolProperty");
        writer.object();
        if (deviceIdentifier != null) {
            writer.key("deviceIdentifier").value(deviceIdentifier.toString());
        }
        if (propertyName != null && propertyValue != null) {
            writer.key(propertyName).value(propertyValue.toString());
        }
        writer.endObject();
    }
}