package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;

import javax.inject.Inject;
import javax.validation.constraints.Size;
import java.time.Instant;

/**
 * Copyrights EnergyICT
 * Date: 10/29/14
 * Time: 2:59 PM
 */
public class DeviceMessageAttributeImpl extends PersistentIdObject<DeviceMessageAttribute> implements DeviceMessageAttribute {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_IS_REQUIRED + "}")
    private Reference<DeviceMessage<Device>> deviceMessage = ValueReference.absent();
    private PropertySpec propertySpec;
    @Size(max= Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    private Object value;
    @Size(max= Table.DESCRIPTION_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String stringValue = ""; // the string representation of the value
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;

    @Inject
    protected DeviceMessageAttributeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(DeviceMessageAttribute.class, dataModel, eventService, thesaurus);
    }

    public DeviceMessageAttributeImpl initialize(DeviceMessage<Device> deviceMessage, String name) {
        this.deviceMessage.set(deviceMessage);
        this.name = name;
        return this;
    }

    @Override
    public PropertySpec getSpecification() {
        if (this.propertySpec == null) {
            this.propertySpec = deviceMessage.get().getSpecification().getPropertySpec(name);
        }
        return propertySpec;
    }

    @Override
    public DeviceMessage<Device> getDeviceMessage() {
        return deviceMessage.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue() {
        if (this.value == null) {
            this.value = getSpecification().getValueFactory().fromStringValue(stringValue);
        }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        try {
            if (getSpecification() != null && getSpecification().validateValue(value)) {  // we do the validation later on ...
                stringValue = getSpecification().getValueFactory().toStringValue(this.value);
            }
        } catch (InvalidValueException e) {
            // absorb ...
        }
    }

    @Override
    protected CreateEventType createEventType() {
        return null;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return null;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return null;
    }

    @Override
    protected void doDelete() {
        // nothing to do ...
    }

    @Override
    protected void validateDelete() {
        // nothing to do ...
    }

    @Override
    public void delete() {
        // we don't delete ourselves ... we get removed out of a collection
    }
}
