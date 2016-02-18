package com.energyict.mdc.engine.impl.events.datastorage;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Copyrights EnergyICT
 * Date: 18/02/2016
 * Time: 13:54
 */
public class UpdateDeviceProtocolPropertyEvent extends AbstractCollectedDataProcessingEventImpl  {

    private final static String DESCRIPTION = "collectedDataProcessingEvent.updateDeviceProtocolProperty.description";

    private final DeviceIdentifier deviceIdentifier;
    private final PropertySpec propertySpec;
    private final Object propertyValue;

    public UpdateDeviceProtocolPropertyEvent(ServiceProvider serviceProvider,
                                             DeviceIdentifier deviceIdentifier,
                                             PropertySpec propertySpec,
                                             Object propertyValue) {
        super(serviceProvider);
        this.deviceIdentifier = deviceIdentifier;
        this.propertySpec = propertySpec;
        this.propertyValue = propertyValue;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("updateDeviceProtocolProperty");
        writer.object();
        if (deviceIdentifier != null) {
            writer.key("deviceIdentifier").value(deviceIdentifier.toString());
        }
        if (propertySpec != null) {
            writer.key("property").value(propertySpec.getDisplayName());
        }
        if (propertyValue != null) {
            writer.key("protocolInfo").value(propertySpec.getValueFactory().toStringValue(propertyValue));
        }
        writer.endObject();
    }
}
