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
/**
DLMS_Key ::= STRUCTURE
        {
        Key_Type:    enumeration,  // 0 for void, 1 for plain-text key, 2 for AESWrap (without padding)
        Key_Data:    DLMS_Key_Data // Key content
        }

 DLMS_Key_Data ::= CHOICE
        {
        No_Key:        null-data,    // In case no key is required
        Plain_Key:     octet-string, / Plain-text key, 128, 192 or 256 bit
        Wrapped-Key:   octet-string  // Wrapped key, 192 bit
        }
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

    /**
     DLMS_Key ::= STRUCTURE
        {
            Key_Type:    enumeration,  // 0 for void, 1 for plain-text key, 2 for AESWrap (without padding)
            Key_Data:    DLMS_Key_Data // Key content
        }
     */
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