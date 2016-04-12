package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 31/03/2016 - 10:26
 */
@XmlRootElement
public class MulticastKey {

    /* 24-byte value (AES key wrapped with DlmsMeterKEK) */
    String wrappedKey;

    public MulticastKey(String wrappedKey) {
        this.wrappedKey = wrappedKey;
    }

    //JSon constructor
    private MulticastKey() {
    }

    public AbstractDataType toDataType() {
        if (getWrappedKey() == null || getWrappedKey().isEmpty()) {
            return new NullData();
        } else {
            return OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getWrappedKey(), ""));
        }
    }

    @XmlAttribute
    public String getWrappedKey() {
        return wrappedKey;
    }
}