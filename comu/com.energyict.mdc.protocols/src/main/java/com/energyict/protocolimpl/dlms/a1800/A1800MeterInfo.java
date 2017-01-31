/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.mdc.common.ObisCode;

import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;

import java.io.IOException;

/**
 * Meter info of A1800
 * <p/>
 * Created by heuckeg on 13.06.2014.
 */
@SuppressWarnings("unused")
public class A1800MeterInfo {

    public static final ObisCode METER_SER_OBIS = ObisCode.fromString("1.0.96.1.0.255");
    public static final ObisCode METER_ID_OBIS = ObisCode.fromString("1.0.96.1.1.255");
    public static final ObisCode METER_FW_OBIS = ObisCode.fromString("1.0.96.1.5.255");
    public static final ObisCode UTILITY_SER_OBIS = ObisCode.fromString("1.0.96.1.2.255");

    private final DlmsSession session;

    private String meterSerial = null;
    private String meterId = null;
    private String utilitySerial = null;
    private String meterVersion = null;

    public A1800MeterInfo(DlmsSession session) {
        this.session = session;
    }

    public String getDeviceSerialNumber() {
        if (meterSerial == null) {
            meterSerial = getDataObject(METER_SER_OBIS);
        }
        return meterSerial;
    }

    public String getMeterId() {
        if (meterId == null) {
            meterId = getDataObject(METER_ID_OBIS);
        }
        return meterId;
    }

    public String getUtilitySerialNumber() {
        if (utilitySerial == null) {
            utilitySerial = getDataObject(UTILITY_SER_OBIS);
        }
        return utilitySerial;
    }

    public String getMeterFirmware() {
        if (meterVersion == null) {
            meterVersion = getDataObject(METER_FW_OBIS);
        }
        return meterVersion;
    }

    private String getDataObject(ObisCode obisCode) {
        try {
            Data d = getCosemObjectFactory().getData(obisCode);
            if (d.getValueAttr().isOctetString()) {
                return ((OctetString) d.getValueAttr()).stringValue().trim();
            }
            return "" + d.getValue();
        } catch (IOException e) {
            session.getLogger().severe("Unable to read data (" + obisCode + ")! [" + e.getMessage() + "]");
        }
        return "?";
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }
}
