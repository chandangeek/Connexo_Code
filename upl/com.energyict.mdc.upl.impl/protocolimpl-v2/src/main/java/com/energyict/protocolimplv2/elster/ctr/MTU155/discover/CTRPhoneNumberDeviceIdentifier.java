package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;


import com.energyict.cbo.NotFoundException;
import com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.DeviceFactory;
import com.energyict.mdw.core.MeteringWarehouse;

import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.protocol.inbound.DeviceIdentifier} interface,
 * specific for the SMS part of the CTR protocol base (MTU155 and EK155 device types). <br></br>
 * SMSes for these protocols are uniquely identified by the devices phone number.
 *
 * @author: sva
 * @since: 26/10/12 (11:26)
 */
public class CTRPhoneNumberDeviceIdentifier implements DeviceIdentifier {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "phoneNumber";

    private final String phoneNumber;

    public CTRPhoneNumberDeviceIdentifier(String phoneNumber) {
        super();
        this.phoneNumber = phoneNumber;
    }

    @Override
    public Device findDevice() {
        DeviceFactory deviceFactory = MeteringWarehouse.getCurrent().getDeviceFactory();
        List<Device> devicesByPhoneNumber = deviceFactory.findByConnectionTypeProperty(InboundProximusSmsConnectionType.class, PHONE_NUMBER_PROPERTY_NAME, phoneNumber);

        if (devicesByPhoneNumber.isEmpty()) {   // Do try with a different phone number format
            devicesByPhoneNumber = deviceFactory.findByConnectionTypeProperty(InboundProximusSmsConnectionType.class, PHONE_NUMBER_PROPERTY_NAME, alterPhoneNumberFormat(phoneNumber));
        }

        if (devicesByPhoneNumber.isEmpty()) {
            throw new NotFoundException("Device with phone number " + this.phoneNumber + " not found");
        } else {
            if (devicesByPhoneNumber.size() > 1) {
                throw new NotFoundException("More than one device found with phone number " + this.phoneNumber);
            } else {
                return devicesByPhoneNumber.get(0);
            }
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

    @Override
    public String getIdentifier() {
        return phoneNumber;
    }
}
