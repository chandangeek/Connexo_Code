package com.energyict.mdc.device.data.impl.identifiers;


import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.exceptions.CanNotFindForIdentifier;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifierType;
import com.energyict.mdc.protocol.api.device.data.identifiers.FindMultipleDevices;
import com.energyict.mdc.protocol.api.exceptions.DuplicateException;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Provides an implementation for the DeviceIdentifier interface,
 * The device can be found based on the given ConnectionType and a property value for that ConnectionType
 */
@XmlRootElement
public class DeviceIdentifierByConnectionTypeAndProperty implements DeviceIdentifier, FindMultipleDevices<Device> {

    private Class<? extends ConnectionType> connectionTypeClass;
    private String propertyName;
    private String propertyValue;
    private DeviceService deviceService;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    public DeviceIdentifierByConnectionTypeAndProperty() {
    }

    public DeviceIdentifierByConnectionTypeAndProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue, DeviceService deviceService) {
        super();
        this.connectionTypeClass = connectionTypeClass;
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.deviceService = deviceService;
    }

    @Override
    public Device findDevice() {
        if(this.device == null){
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
        this.allDevices = this.deviceService.findDevicesByConnectionTypeAndProperty(connectionTypeClass, propertyName, propertyValue);
    }

    /**
     * Replace +XY by 0, e.g. +32 = 0, +39 = 0
     *
     * @param propertyValue: a given telephone number
     * @return the modified telephone number
     */
    protected String alterPhoneNumberFormat(String propertyValue) {
        if (propertyValue.length() > 3) {
            if ("+".equals(Character.toString(propertyValue.charAt(0)))) {
                propertyValue = "0" + propertyValue.substring(3);
            }
        }
        return propertyValue;
    }

    @Override
    public String toString() {
        return "device with connectiontype" + this.connectionTypeClass.getName() + " and property '" + this.propertyName + "' and value '" + this.propertyValue + "'";
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public String getPropertyValue() {
        return propertyValue;
    }

    @Override
    public String getIdentifier() {
        return propertyValue;
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.Other;
    }

    @Override
    public List<Device> getAllDevices() {
        if(this.allDevices == null){
            fetchAllDevices();
        }
       return this.allDevices;
    }
}
