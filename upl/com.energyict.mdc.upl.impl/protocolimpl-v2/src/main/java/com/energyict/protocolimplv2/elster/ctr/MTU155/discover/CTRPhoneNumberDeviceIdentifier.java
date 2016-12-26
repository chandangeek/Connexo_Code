package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.channels.sms.InboundProximusSmsConnectionType;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.FindMultipleDevices;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides an implementation for the {@link DeviceIdentifier} interface,
 * specific for the SMS part of the CTR protocol base (MTU155 and EK155 device types). <br></br>
 * SMSes for these protocols are uniquely identified by the devices phone number.
 *
 * @author sva
 * @since 26/10/12 (11:26)
 */
@XmlRootElement
public class CTRPhoneNumberDeviceIdentifier implements FindMultipleDevices {

    public static final String PHONE_NUMBER_PROPERTY_NAME = "phoneNumber";

    private final String phoneNumber;

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

    @XmlAttribute
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public com.energyict.mdc.upl.meterdata.identifiers.Introspector forIntrospection() {
        return new Introspector();
    }

    private class Introspector implements com.energyict.mdc.upl.meterdata.identifiers.Introspector {
        @Override
        public String getTypeName() {
            return "PhoneNumber";
        }

        @Override
        public Object getValue(String role) {
            if ("phoneNumber".equals(role)) {
                return phoneNumber;
            } else if ("propertyName".equals(role)) {
                return PHONE_NUMBER_PROPERTY_NAME;
            } else if ("connectionTypeClass".equals(role)) {
                return InboundProximusSmsConnectionType.class;
            } else {
                throw new IllegalArgumentException("Role '" + role + "' is not supported by identifier of type " + getTypeName());
            }
        }

    }

}