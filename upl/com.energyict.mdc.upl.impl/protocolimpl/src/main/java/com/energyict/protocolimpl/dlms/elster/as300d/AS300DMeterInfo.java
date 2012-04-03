package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 22/02/12
 * Time: 15:20
 */
public class AS300DMeterInfo {

    public static final ObisCode PRIME_FW_OBIS = ObisCode.fromString("0.0.28.7.0.255");
    public static final ObisCode AS300_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode AS300_SERIAL = ObisCode.fromString("0.0.96.1.0.255");

    private final DlmsSession session;

    public AS300DMeterInfo(DlmsSession session) {
        this.session = session;
    }

    public String getAS300DFirmware() {
        try {
            Data data = getCosemObjectFactory().getData(AS300_FIRMWARE);
            return data.getString();
        } catch (IOException e) {
            session.getLogger().severe("Unable to read AS300D firmware version! [" + e.getMessage() + "]");
            return "";
        }
    }

    public String getPrimeFirmware() {
        try {
            GenericRead genericRead = getCosemObjectFactory().getGenericRead(PRIME_FW_OBIS, 1 * 8, 86);
            byte[] responseData = genericRead.getResponseData();
            return AXDRDecoder.decode(responseData).getOctetString().stringValue();
        } catch (IOException e) {
            session.getLogger().severe("Unable to read PRIME firmware version! [" + e.getMessage() + "]");
            return "";
        }
    }

    public String getAllFirmwareVersions() {
        StringBuilder sb = new StringBuilder();
        sb.append("AS300D=[").append(getAS300DFirmware().trim()).append("], ");
        sb.append("Prime=[").append(getPrimeFirmware().trim()).append("]");
        return sb.toString();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }

    public String getMeterSerialNumber() throws NestedIOException {
        try {
            Data data = getCosemObjectFactory().getData(AS300_SERIAL);
            return data.getString();
        } catch (IOException e) {
            throw new NestedIOException(e, "Unable to read AS300D serial number!");
        }
    }
}
