/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.prime;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.GenericRead;

import java.io.IOException;

public class PrimeMeterInfo {

    public static final ObisCode PRIME_FW_OBIS = ObisCode.fromString("0.0.28.7.0.255");
    public static final ObisCode DLMS_FIRMWARE = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode DLMS_SERIAL = ObisCode.fromString("0.0.96.1.0.255");

    private final DlmsSession session;

    private String dlmsVersion = null;
    private String primeVersion = null;
    private String serialNumber = null;

    public PrimeMeterInfo(DlmsSession session) {
        this.session = session;
    }

    public String getAllFirmwareVersions() {
        StringBuilder sb = new StringBuilder();
        sb.append("DLMS=[").append(getDLMSFirmware().trim()).append("], ");
        sb.append("Prime=[").append(getPrimeFirmware().trim()).append("]");
        return sb.toString();
    }

    public String getDLMSFirmware() {
        if (dlmsVersion == null) {
            try {
                final Data data = getCosemObjectFactory().getData(DLMS_FIRMWARE);
                this.dlmsVersion = data.getString().trim();
            } catch (IOException e) {
                session.getLogger().severe("Unable to read DLMS firmware version! [" + e.getMessage() + "]");
            }
        }
        return this.dlmsVersion == null ? "" : dlmsVersion;
    }

    public String getPrimeFirmware() {
        if (primeVersion == null) {
            try {
                final GenericRead genericRead = getCosemObjectFactory().getGenericRead(PRIME_FW_OBIS, 1 * 8, 86);
                final byte[] responseData = genericRead.getResponseData();
                final OctetString firmwareVersion = AXDRDecoder.decode(responseData, OctetString.class);
                this.primeVersion = firmwareVersion.stringValue().trim();
            } catch (IOException e) {
                session.getLogger().severe("Unable to read PRIME firmware version! [" + e.getMessage() + "]");
            }
        }
        return this.primeVersion == null ? "" : primeVersion;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }

    //public final boolean isManufacturer(final PrimeManufacturer manufacturer) throws IOException {
    //    Data data = getCosemObjectFactory().getData(ObisCode.fromString("0.0.96.1.4.255"));
    //    String serial = ((OctetString) data.getValueAttr()).stringValue();
    //    session.getLogger().info("Extended serial number: " + serial);
    //    return manufacturer.matches(serial);
    //}

    public String getMeterSerialNumber() throws IOException {
        if (this.serialNumber == null) {
            try {
                Data data = getCosemObjectFactory().getData(DLMS_SERIAL);
                this.serialNumber = data.getString().trim();
            } catch (IOException e) {
                throw new NestedIOException(e, "Unable to read DLMS serial number! [" + e.getMessage() + "]");
            }
        }
        return this.serialNumber == null ? "" : serialNumber;
    }

}
