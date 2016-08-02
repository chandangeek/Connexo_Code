package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 25/03/2016 - 17:15
 */
@XmlRootElement
public class MulticastMeterConfig {

    /* Device identifier (EUI-64) */
    String deviceIdentifier;
    /* Meter serial */
    String meterSerialNumber;
    /* Unicast security configuration */
    MulticastKeySet unicastSecurity;
    /* Multicast security configuration */
    MulticastKeySet multicastSecurity;

    //JSon constructor
    private MulticastMeterConfig() {
    }

    public MulticastMeterConfig(String deviceIdentifier, String meterSerialNumber, MulticastKeySet unicastSecurity, MulticastKeySet multicastSecurity) {
        this.deviceIdentifier = deviceIdentifier;
        this.meterSerialNumber = meterSerialNumber;
        this.unicastSecurity = unicastSecurity;
        this.multicastSecurity = multicastSecurity;
    }

    public Structure toStructure() {
        final Structure result = new Structure();
        result.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getDeviceIdentifier(), "")));
        result.addDataType(OctetString.fromString(getMeterSerialNumber()));
        result.addDataType(getUnicastSecurity().toDataType());
        result.addDataType(getMulticastSecurity().toDataType());
        return result;
    }

    @XmlAttribute
    public String getDeviceIdentifier() {
        return deviceIdentifier;
    }

    @XmlAttribute
    public String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    @XmlAttribute
    public MulticastKeySet getUnicastSecurity() {
        return unicastSecurity;
    }

    @XmlAttribute
    public MulticastKeySet getMulticastSecurity() {
        return multicastSecurity;
    }
}