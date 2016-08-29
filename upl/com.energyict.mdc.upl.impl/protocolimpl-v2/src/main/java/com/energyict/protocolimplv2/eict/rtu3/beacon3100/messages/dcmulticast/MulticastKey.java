package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.dlms.axrdencoding.*;
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
    private static final int DLMS_KEY_TYPE_VOID = 0;
    private static final int DLMS_KEY_TYPE_PLAIN_TEXT = 1;
    private static final int DLMS_KEY_TYPE_AESWRAP_WITHOUT_PADDING = 2;


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
            final Structure result = new Structure();
            result.addDataType(new TypeEnum(DLMS_KEY_TYPE_AESWRAP_WITHOUT_PADDING));
            result.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getWrappedKey(), "")));
            return result;
        }
    }

    @XmlAttribute
    public String getWrappedKey() {
        return wrappedKey;
    }
}