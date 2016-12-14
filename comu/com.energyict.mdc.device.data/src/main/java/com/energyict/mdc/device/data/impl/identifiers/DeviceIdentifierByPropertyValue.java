package com.energyict.mdc.device.data.impl.identifiers;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Provides an implementation for the DeviceIdentifier interface,
 * The device can be found based on the given propertyname and propertyvalue.
 * Note that multiple devices can be found with the provided combinations
 */
@XmlRootElement
public class DeviceIdentifierByPropertyValue implements DeviceIdentifier, FindMultipleDevices {

    private String propertyName;
    private String propertyValue;
    private DeviceService deviceService;

    private Device device;
    private List<com.energyict.mdc.device.data.Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierByPropertyValue() {
    }

    public DeviceIdentifierByPropertyValue(String propertyName, String callHomeId, DeviceService deviceService) {
        this();
        this.propertyName = propertyName;
        this.propertyValue = callHomeId;
        this.deviceService = deviceService;
    }

    @Override
    public Device findDevice() {
        if (this.device == null) {
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw CanNotFindForIdentifier.device(this, MessageSeeds.CAN_NOT_FIND_FOR_DEVICE_IDENTIFIER);
            } else {
                if (this.allDevices.size() > 1) {
                    throw new DuplicateException(MessageSeeds.DUPLICATE_FOUND, Device.class, this.toString());
                } else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices() {
        this.allDevices = this.deviceService.findDevicesByPropertySpecValue(propertyName, propertyValue);
    }

    @Override
    public String toString() {
        return "device having property '" + this.propertyName + "' and value '" + this.propertyValue + "'";
    }

    @XmlAttribute
    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    @Override
    public List<com.energyict.mdc.device.data.Device> getAllDevices() {
        if(this.allDevices == null){
            fetchAllDevices();
        }
        return this.allDevices;
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "PropertyBased";
        }

        @Override
        public Object getValue(String role) {
            switch (role) {
                case "propertyName": {
                    return propertyName;
                }
                case "propertyValue": {
                    return propertyValue;
                }
                default: {
                    throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
                }
            }
        }
    }

}