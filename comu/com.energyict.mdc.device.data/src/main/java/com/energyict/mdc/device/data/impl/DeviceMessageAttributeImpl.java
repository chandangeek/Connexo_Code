package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.exceptions.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageAttribute;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 10/29/14
 * Time: 2:59 PM
 */
public class DeviceMessageAttributeImpl<T> extends PersistentIdObject<DeviceMessageAttribute> implements DeviceMessageAttribute<T> {

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.DEVICE_MESSAGE_IS_REQUIRED + "}")
    private Reference<DeviceMessage<Device>> deviceMessage = ValueReference.absent();
    private PropertySpec<T> propertySpec;
    private String name;
    private T value;
    private String stringValue = ""; // the string representation of the value

    @Inject
    protected DeviceMessageAttributeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus) {
        super(DeviceMessageAttribute.class, dataModel, eventService, thesaurus);
    }

    public DeviceMessageAttributeImpl<T> initialize(DeviceMessage<Device> deviceMessage, String name) {
        this.deviceMessage.set(deviceMessage);
        this.name = name;
        return this;
    }

    @Override
    public PropertySpec<T> getSpecification() {
        if(this.propertySpec == null){
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
    public T getValue() {
        if(this.value == null){
            this.value = getSpecification().getValueFactory().fromStringValue(stringValue);
        }
        return value;
    }

    public void setValue(T value) {
        this.value = value;
        try {
            if(getSpecification() != null && getSpecification().validateValue(value)){  // we do the validation later on ...
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
