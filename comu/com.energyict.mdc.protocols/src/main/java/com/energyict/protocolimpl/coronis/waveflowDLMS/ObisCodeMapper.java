/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ObisCodeMapper {


    private final AbstractDLMS abstractDLMS;

    private final int CLASS_DATA = 1;
    private final int CLASS_REGISTER = 3;
    private final int CLASS_EXTENDED_REGISTER = 4;

    private final int ATTRIBUTE_VALUE = 2;
    private final int ATTRIBUTE_SCALER = 3;
    private final int ATTRIBUTE_CAPTURETIME = 5;

    /**
     * A
     */
    int entryCount = 0;

    /**
     * cached registervalues read by the Transparant list object list reader using the property ObisCodeList
     */
    private Map<ObisCode, RegisterValue> cachedRegisterValues = new HashMap<ObisCode, RegisterValue>();

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public ObisCodeMapper(final AbstractDLMS abstractDLMS) {
        this.abstractDLMS = abstractDLMS;
    }

    final String getRegisterExtendedLogging() {

        StringBuilder strBuilder = new StringBuilder();

        Iterator<Entry<ObisCode, ObjectEntry>> it = abstractDLMS.getObjectEntries().entrySet().iterator();
        while (it.hasNext()) {
            Entry<ObisCode, ObjectEntry> o = it.next();
            strBuilder.append(o.getKey().toString() + ", " + o.getValue().getDescription() + "\n");
        }
        strBuilder.append("0.0.96.1.0.255" + ", firmware version\n");
        strBuilder.append("0.0.96.2.0.255" + ", operation mode\n");
        strBuilder.append("0.0.96.3.0.255" + ", application status\n");
        strBuilder.append("0.0.96.4.0.255" + ", alarm configuration\n");
        strBuilder.append("0.0.96.5.0.255" + ", RSSI level\n");


        // special obis code to control the waveflow RTC
        strBuilder.append("0.0.96.6.200.255" + ", set the waveflow clock\n");
        strBuilder.append("0.0.96.6.201.255" + ", read the waveflow clock\n");


        return strBuilder.toString();
    }

    public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {


        if (obisCode.equals(ObisCode.fromString("0.0.96.1.0.255"))) {
            return new RegisterInfo("firmware version");
        }
        if (obisCode.equals(ObisCode.fromString("0.0.96.2.0.255"))) {
            return new RegisterInfo("operation mode");
        }
        if (obisCode.equals(ObisCode.fromString("0.0.96.3.0.255"))) {
            return new RegisterInfo("application status");
        }
        if (obisCode.equals(ObisCode.fromString("0.0.96.4.0.255"))) {
            return new RegisterInfo("alarm configuration");
        }
        if (obisCode.equals(ObisCode.fromString("0.0.96.5.0.255"))) {
            return new RegisterInfo("RSSI level");
        }


        // special obis code to control the waveflow RTC
        if (obisCode.equals(ObisCode.fromString("0.0.96.6.200.255"))) {
            return new RegisterInfo("set the waveflow clock");
        }
        if (obisCode.equals(ObisCode.fromString("0.0.96.6.201.255"))) {
            return new RegisterInfo("read the waveflow clock");
        }


        return new RegisterInfo(abstractDLMS.findObjectByObiscode(obisCode).getDescription());
    }

    final private void transparantReadList(final List<ObjectInfo> objectInfoList) throws IOException {
        TransparentObjectListRead transparentObjectListRead = new TransparentObjectListRead(abstractDLMS, objectInfoList, cachedRegisterValues);
        transparentObjectListRead.read();
        abstractDLMS.getLogger().info("Cached [" + objectInfoList.size() + "] attributes...");
    }

    final private List<ObjectInfo> readAttributes(final List<ObjectInfo> objectInfoList) throws IOException {
        if (++entryCount >= 16) {
            transparantReadList(objectInfoList);
            entryCount = 0;
            return new ArrayList<ObjectInfo>();
        } else {
            return objectInfoList;
        }
    }

    public void cacheRegisters(final List<ObisCode> obisCodes) throws IOException {


        List<ObjectInfo> objectInfoList = new ArrayList<ObjectInfo>();

        for (ObisCode obisCode : obisCodes) {
            try {
                ObjectEntry objectEntry = abstractDLMS.findObjectByObiscode(obisCode);

                // if obis code found in register list for meter, check class id and fill 0x36 list according to the attributs to request
                switch (objectEntry.getClassId()) {

                    case CLASS_DATA: {
                        objectInfoList.add(new ObjectInfo(ATTRIBUTE_VALUE, CLASS_DATA, obisCode));
                        objectInfoList = readAttributes(objectInfoList);
                    }
                    break; // CLASS_DATA

                    case CLASS_REGISTER: {
                        objectInfoList.add(new ObjectInfo(ATTRIBUTE_VALUE, CLASS_REGISTER, obisCode));
                        objectInfoList = readAttributes(objectInfoList);
                        objectInfoList.add(new ObjectInfo(ATTRIBUTE_SCALER, CLASS_REGISTER, obisCode));
                        objectInfoList = readAttributes(objectInfoList);
                    }
                    break; // CLASS_REGISTER

                    case CLASS_EXTENDED_REGISTER: {
                        objectInfoList.add(new ObjectInfo(ATTRIBUTE_VALUE, CLASS_EXTENDED_REGISTER, obisCode));
                        objectInfoList = readAttributes(objectInfoList);
                        objectInfoList.add(new ObjectInfo(ATTRIBUTE_SCALER, CLASS_EXTENDED_REGISTER, obisCode));
                        objectInfoList = readAttributes(objectInfoList);
                    }
                    break; // CLASS_REGISTER

                    default: {

                    }
                    break;

                } // switch(objectEntry.getClassId())
            } catch (NoSuchRegisterException e) {
                // absorb will be read by local API or 0x31 method
            }

        } // for (ObisCode obisCode : obisCodes)

        if (objectInfoList.size() > 0) {
            transparantReadList(objectInfoList);
        }

    } // public void cacheRegisters(final List<ObisCode> obisCodes) throws IOException

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {


        // common non-register obis codes
        if (obisCode.equals(ObisCode.fromString("0.0.96.1.0.255"))) { //firmware version
            return new RegisterValue(obisCode, "V" + WaveflowProtocolUtils.toHexString(abstractDLMS.getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion()) + ", Mode of transmission " + abstractDLMS.getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission());
        }
        if (obisCode.equals(ObisCode.fromString("0.0.96.2.0.255"))) { //Operation mode
            return new RegisterValue(obisCode, new Quantity("" + abstractDLMS.getParameterFactory().readOperatingMode(), Unit.get("")));
//    		if (abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader() == null) {
//    			abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(abstractDLMS.CLOCK_OBIS_CODE);
//    		}
//   			return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader().getOperatingMode(),Unit.get("")));
        }
        if (obisCode.equals(ObisCode.fromString("0.0.96.3.0.255"))) { //Application status
            return new RegisterValue(obisCode, new Quantity("" + abstractDLMS.getParameterFactory().readApplicationStatus(), Unit.get("")));
//    		if (abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader() == null) {
//    			abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(abstractDLMS.CLOCK_OBIS_CODE);
//    		}
//       		return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader().getApplicationStatus(),Unit.get("")));
        }
        if (obisCode.equals(ObisCode.fromString("0.0.96.4.0.255"))) { //Alarm Configuration
            return new RegisterValue(obisCode, new Quantity("" + abstractDLMS.getParameterFactory().readAlarmConfiguration(), Unit.get("")));
//    		if (abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader() == null) {
//    			abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(abstractDLMS.CLOCK_OBIS_CODE);
//    		}
//       		return new RegisterValue(obisCode,new Quantity(""+abstractDLMS.getTransparantObjectAccessFactory().getGenericHeader().getAlarmConfiguration(),Unit.get("")));
        }

        if (obisCode.equals(ObisCode.fromString("0.0.96.5.0.255"))) { //RSSI level
            return new RegisterValue(obisCode, new Quantity(abstractDLMS.getRadioCommandFactory().readRSSILevel(), Unit.get("")));
        }

        if (obisCode.equals(ObisCode.fromString("0.0.96.6.200.255"))) {
            try {
                abstractDLMS.setWaveFlowTime();
                return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "WaveFlow AC RTC set successfull.");
            } catch (IOException e) {
                return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "Error setting the WaveFlow AC RTC [" + e.getMessage() + "]");
            }
        }

        if (obisCode.equals(ObisCode.fromString("0.0.96.6.201.255"))) {
            Date date = abstractDLMS.getParameterFactory().readTimeDateRTC();
            return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "" + date);
        }

        boolean timeRegister = obisCode.equals(ObisCode.fromString("1.1.0.9.1.255"));
        boolean dateRegister = obisCode.equals(ObisCode.fromString("1.1.0.9.2.255"));
        if (timeRegister || dateRegister) {
            Date time = abstractDLMS.getTime();
            SimpleDateFormat formatter = new SimpleDateFormat(dateRegister ? "yyyy-MM-dd" : "HH:mm:ss");
            return new RegisterValue(obisCode, formatter.format(time));
        }

        ObjectEntry objectEntry = abstractDLMS.findObjectByObiscode(obisCode);

        RegisterValue registerValue = cachedRegisterValues.get(obisCode);
        if (registerValue != null) {
            abstractDLMS.getLogger().info("Read " + obisCode + " from cached values");
            return registerValue;
        }

        try {
            if (objectEntry.getClassId() == CLASS_DATA) {
                AbstractDataType adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectValue(obisCode);

                if (obisCode.equals(abstractDLMS.getSerialNumberObisCodeForPairingRequest())) {
                    String serialNumber;
                    if (adt.isOctetString()) {
                        serialNumber = adt.getOctetString().stringValue();
                    } else {
                        serialNumber = pad(String.valueOf(adt.longValue()), 8);
                    }
                    return new RegisterValue(obisCode, serialNumber);
                }

                if (adt.isOctetString()) {
                    if (obisCode.equals(ObisCode.fromString("1.1.96.3.0.255")) || obisCode.equals(ObisCode.fromString("1.1.97.97.1.255")) || obisCode.equals(ObisCode.fromString("1.1.97.97.2.255")) ||
                            obisCode.equals(ObisCode.fromString("1.1.97.97.255.255")) || obisCode.equals(ObisCode.fromString("1.1.97.97.3.255"))) {
                        return new RegisterValue(obisCode, ProtocolUtils.outputHexString(adt.getOctetString().getOctetStr()));
                    } else {
                        return new RegisterValue(obisCode, adt.getOctetString().stringValue() + " [" + ProtocolUtils.outputHexString(adt.getOctetString().getOctetStr()) + "]");
                    }
                } else if (adt.isVisibleString()) {
                    return new RegisterValue(obisCode, adt.getVisibleString().getStr());
                } else {
                    return new RegisterValue(obisCode, new Quantity(adt.toBigDecimal(), Unit.get("")));
                }
            } else if (objectEntry.getClassId() == CLASS_REGISTER) {

                AbstractDataType adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_SCALER);
                int scale = adt.getStructure().getDataType(0).intValue();
                int code = adt.getStructure().getDataType(1).intValue();
                Unit unit = Unit.get(code, scale);

                adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_VALUE);
                BigDecimal value = adt.toBigDecimal();

                return new RegisterValue(obisCode, new Quantity(value, unit));
            } else if (objectEntry.getClassId() == CLASS_EXTENDED_REGISTER) {

                AbstractDataType adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_SCALER);
                int scale = adt.getStructure().getDataType(0).intValue();
                int code = adt.getStructure().getDataType(1).intValue();
                Unit unit = Unit.get(code, scale);

                adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_VALUE);
                BigDecimal value = adt.toBigDecimal();

                adt = abstractDLMS.getTransparantObjectAccessFactory().readObjectAttribute(obisCode, ATTRIBUTE_CAPTURETIME);

                DateTime eventTime = new DateTime(adt.getOctetString(), abstractDLMS.getTimeZone());

                return new RegisterValue(obisCode, new Quantity(value, unit), eventTime.getValue().getTime());
            }
        } catch (DataAccessResultException e) {
            if ((e.getCode() == e.getCode().OBJECT_UNAVAILABLE) ||
                    (e.getCode() == e.getCode().OBJECT_UNDEFINED)) {
                throw new NoSuchRegisterException("Register with obis code [" + obisCode + "] does not exist [" + e.getMessage() + "]!");
            } else {
                throw e;
            }
        }

        throw new NoSuchRegisterException("Register with obis code [" + obisCode + "] does not exist!"); // has an error ["+e.getMessage()+"]!");
    }

    private String pad(String serialNumber, int length) {
        while (serialNumber.length() < length) {
            serialNumber = "0" + serialNumber;
        }
        return serialNumber;
    }
}