package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.firmwareobjects;

import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.security.SecurityProperty;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 13/08/2015 - 15:13
 */
@XmlRootElement
public class DeviceInfo {

    private TypedProperties generalProperties;
    private TypedProperties dialectProperties;
    private List<SecurityProperty> securityProperties;
    private int deviceID;

    public DeviceInfo(TypedProperties generalProperties, TypedProperties dialectProperties, List<SecurityProperty> securityProperties, int deviceID) {
        this.generalProperties = generalProperties;
        this.dialectProperties = dialectProperties;
        this.securityProperties = securityProperties;
        this.deviceID = deviceID;
    }

    private DeviceInfo() {
        //JSon only
    }

    @XmlAttribute
    public int getDeviceID() {
        return deviceID;
    }

    @XmlAttribute
    public TypedProperties getGeneralProperties() {
        return generalProperties;
    }

    @XmlAttribute
    public TypedProperties getDialectProperties() {
        return dialectProperties;
    }

    @XmlAttribute
    public List<SecurityProperty> getSecurityProperties() {
        return securityProperties;
    }
}