package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:07
 */
@XmlRootElement
public class RTU3MeterDetails {

    private String macAddress;
    private long deviceTypeId;
    private String llsSecret;
    private String authenticationKey;
    private String encryptionKey;

    public RTU3MeterDetails(String macAddress, long deviceTypeId, String llsSecret, String authenticationKey, String encryptionKey) {
        this.macAddress = macAddress;
        this.deviceTypeId = deviceTypeId;
        this.llsSecret = llsSecret;
        this.authenticationKey = authenticationKey;
        this.encryptionKey = encryptionKey;
    }

    //JSon constructor
    private RTU3MeterDetails() {
    }

    @XmlAttribute
    public String getMacAddress() {
        return macAddress;
    }

    @XmlAttribute
    public long getDeviceTypeId() {
        return deviceTypeId;
    }

    @XmlAttribute
    public String getLlsSecret() {
        return llsSecret;
    }

    @XmlAttribute
    public String getAuthenticationKey() {
        return authenticationKey;
    }

    @XmlAttribute
    public String getEncryptionKey() {
        return encryptionKey;
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getMacAddress(), "")));
        structure.addDataType(new Unsigned32(getDeviceTypeId()));
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getLlsSecret(), "")));
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getAuthenticationKey(), "")));
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getEncryptionKey(), "")));
        return structure;
    }
}