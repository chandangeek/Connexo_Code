package com.energyict.protocolimplv2.identifiers;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Holder for the call home ID, this is the same as the serial number
 * <p/>
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 11:49 AM
 */
@XmlRootElement
public class CallHomeIdPlaceHolder {

    private String serialNumber;

    public CallHomeIdPlaceHolder() {
    }

    @XmlAttribute
    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }
}
