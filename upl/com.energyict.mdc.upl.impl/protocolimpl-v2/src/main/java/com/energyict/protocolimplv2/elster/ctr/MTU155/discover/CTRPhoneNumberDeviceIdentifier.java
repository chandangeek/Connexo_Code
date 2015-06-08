package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;


import com.energyict.cbo.NotFoundException;
import com.energyict.cpo.OfflineDeviceContext;
import com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.mdc.protocol.inbound.DeviceIdentifierType;
import com.energyict.mdc.protocol.inbound.ServerDeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.DeviceFactoryProvider;
import com.energyict.mdw.core.DeviceOfflineFlags;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocolimplv2.MdcManager;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface,
 * specific for the SMS part of the CTR protocol base (MTU155 and EK155 device types). <br></br>
 * SMSes for these protocols are uniquely identified by the devices phone number.
 *
 * @author: sva
 * @since: 26/10/12 (11:26)
 */
@XmlRootElement
public class CTRPhoneNumberDeviceIdentifier implements ServerDeviceIdentifier {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "phoneNumber";

    private final String phoneNumber;
    private Device device;
    private List<Device> allDevices;

    /**
     * Constructor only to be used by JSON (de)marshalling
     */
    private CTRPhoneNumberDeviceIdentifier() {
        phoneNumber = "";
    }

    public CTRPhoneNumberDeviceIdentifier(String phoneNumber) {
        super();
        this.phoneNumber = phoneNumber;
    }

    @Override
    public Device findDevice() {
        if(this.device == null){
            fetchAllDevices();
            if (this.allDevices.isEmpty()) {
                throw new NotFoundException("Device with phone number " + this.phoneNumber + " not found");
            } else {
                if (this.allDevices.size() > 1) {
                    throw MdcManager.getComServerExceptionFactory().createDuplicateException(Device.class, this.toString());
                } else {
                    this.device = this.allDevices.get(0);
                }
            }
        }
        return this.device;
    }

    private void fetchAllDevices() {
        this.allDevices = getDeviceFactory().findByConnectionTypeProperty(InboundProximusSmsConnectionType.class, PHONE_NUMBER_PROPERTY_NAME, phoneNumber);
        if (this.allDevices.isEmpty()) {   // Do try with a different phone number format
            this.allDevices = getDeviceFactory().findByConnectionTypeProperty(InboundProximusSmsConnectionType.class, PHONE_NUMBER_PROPERTY_NAME, alterPhoneNumberFormat(phoneNumber));
        }
    }

    /**
     * Replace +XY by 0, e.g. +32 = 0, +39 = 0
     *
     * @param phoneNumber: a given telephone number
     * @return the modified telephone number
     */
    protected String alterPhoneNumberFormat(String phoneNumber) {
        if (phoneNumber.length() > 3) {
            if ("+".equals(Character.toString(phoneNumber.charAt(0)))) {
                phoneNumber = "0" + phoneNumber.substring(3);
            }
        }
        return phoneNumber;
    }

    @Override
    public String toString() {
        return "device with phone number " + this.phoneNumber;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    @XmlAttribute
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String getIdentifier() {
        return phoneNumber;
    }

    @Override
    public DeviceIdentifierType getDeviceIdentifierType() {
        return DeviceIdentifierType.Other;
    }

    @Override
    public List<OfflineDevice> getAllDevices() {
        if(this.allDevices == null){
            fetchAllDevices();
        }
        List<OfflineDevice> allOfflineDevices = new ArrayList<>();
        OfflineDeviceContext offlineDeviceContext = new DeviceOfflineFlags();
        for (Device deviceToGoOffline : this.allDevices) {
            allOfflineDevices.add(deviceToGoOffline.goOffline(offlineDeviceContext));
        }
        return allOfflineDevices;
    }

    private DeviceFactory getDeviceFactory() {
        return DeviceFactoryProvider.instance.get().getDeviceFactory();
    }
}
