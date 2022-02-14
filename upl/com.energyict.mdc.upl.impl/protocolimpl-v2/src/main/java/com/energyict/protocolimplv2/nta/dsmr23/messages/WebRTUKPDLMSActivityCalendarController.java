package com.energyict.protocolimplv2.nta.dsmr23.messages;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.dlms.common.DLMSActivityCalendarController;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Level;

public class WebRTUKPDLMSActivityCalendarController extends DLMSActivityCalendarController {

    public WebRTUKPDLMSActivityCalendarController(CosemObjectFactory cosemObjectFactory, TimeZone timeZone, boolean xmlContentEncodedAsBase64) {
        super(cosemObjectFactory, timeZone, xmlContentEncodedAsBase64);
    }

    /**
     * Create an OctetString from an index. The value of the OctetString should be the byteValue of the given String.
     *
     * @param index the Index to convert to OctetString
     * @param type  the type (0 for seasonName; 1 for weekProfileName)
     * @return the constructed OctetString
     * @throws java.io.IOException if the size of the index is not equal to 1
     */

    @Override
    protected OctetString createByteName(String index, int type) throws IOException {
        byte[] content = new byte[index.length()];
        if (content.length == 1) {
            if (type==0) {
                content[0] = 0x31;
            }
            if (type==1) {
                content[0] = 0x30;
            }
        } else {
            logger.log(Level.WARNING, errors[type]);
            throw new IOException(errors[type]);
        }
        return OctetString.fromByteArray(content);
    }
}
