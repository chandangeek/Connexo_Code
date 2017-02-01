/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.datastorage;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.engine.events.CollectedDataProcessingEvent;
import com.energyict.mdc.engine.impl.commands.store.UpdateDeviceProtocolProperty;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * {@link CollectedDataProcessingEvent} related to a {@link UpdateDeviceProtocolProperty}
 */
public class UpdateDeviceProtocolPropertyEvent extends AbstractCollectedDataProcessingEventImpl  {

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
        return UpdateDeviceProtocolProperty.DESCRIPTION_TITLE;
    }

    protected void addPayload(JSONWriter writer) throws JSONException {
        writer.key("updateDeviceProtocolProperty");
        writer.object();
        if (deviceIdentifier != null) {
            writer.key("deviceIdentifier").value(deviceIdentifier.toString());
        }
        if (propertySpec != null && propertyValue != null) {
            writer.key(propertySpec.getDisplayName()).value(propertySpec.getValueFactory().toStringValue(propertyValue));
        }
        writer.endObject();
    }
}
