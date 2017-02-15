/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SingleActionScheduleMapping extends G3Mapping {

    private static final Map<Integer, String> modes = new HashMap<Integer, String>();

    static {
        modes.put(1, "Mode 1: Number of \"execution time\" = 1 and wildcard in the date is allowed.");
        modes.put(2, "Mode 2: Number of \"execution time\" = n, all time values are the same and wildcards in the date are not allowed.");
        modes.put(3, "Mode 3: Number of \"execution time\" = n, all time values are the same and wildcards in the date are allowed.");
        modes.put(4, "Mode 4: Number of \"execution time\" = n, all time values may be different and wildcards in the date are not allowed.");
        modes.put(5, "Mode 5: Number of \"execution time\" = n, all time values may be different and wildcards in the date are allowed.");
    }

    private final TimeZone timeZone;

    public SingleActionScheduleMapping(TimeZone timeZone, ObisCode obisCode) {
        super(obisCode);
        this.timeZone = timeZone;
    }

    @Override
    public ObisCode getBaseObisCode() {                 //Set the E-field to 0
        return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 4, (byte) 0);
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.SINGLE_ACTION_SCHEDULE.getClassId();
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        ObisCode objectObisCode = ProtocolTools.setObisCodeField(getObisCode(), 4, (byte) 0);
        SingleActionSchedule singleActionSchedule = cosemObjectFactory.getSingleActionSchedule(objectObisCode);

        if (getObisCode().getE() == 1) {                //Type
            return parse(singleActionSchedule.getType(), null, null);
        } else if (getObisCode().getE() == 2) {         //Execution time
            return parse(null, null, getExecutionTime(timeZone, singleActionSchedule.getExecutionTime()));
        } else if (getObisCode().getE() == 0) {         //Both
            return parse(singleActionSchedule.getType(), null, getExecutionTime(timeZone, singleActionSchedule.getExecutionTime()));
        }
        throw new NoSuchRegisterException(getObisCode().toString());
    }

    @Override
    public int[] getAttributeNumbers() {
        if (getObisCode().getE() == 1) {
            return new int[]{3};
        } else if (getObisCode().getE() == 2) {
            return new int[]{4};
        } else if (getObisCode().getE() == 0) {
            return new int[]{3, 4};   //2 attributes
        }
        return new int[]{3};
    }

    /**
     * Special case: if the execution time is requested, it is passed on to this method in the capture time parameter.
     * This way we don't have to pass on a timezone parameter or multiple abstractDataTypes
     */
    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        if (getObisCode().getE() == 1) {                //Type
            int mode = ((TypeEnum) abstractDataType).getValue();
            String modeDescription = modes.get(mode);
            return new RegisterValue(getObisCode(), modeDescription == null ? ("Unknown type: " + mode) : modeDescription);
        } else if (getObisCode().getE() == 2) {         //Execution time
            String executionTimeDescription = "Execution time: " + (captureTime == null ? "default" : captureTime.toString());
            return new RegisterValue(getObisCode(), executionTimeDescription);
        } else if (getObisCode().getE() == 0) {         //Both
            String executionTimeDescription = "Execution time: " + (captureTime == null ? "default" : captureTime.toString());
            return new RegisterValue(getObisCode(), "Mode: " + String.valueOf(((TypeEnum) abstractDataType).getValue()) + ", " + executionTimeDescription);
        }
        throw new NoSuchRegisterException(getObisCode().toString());
    }

    public Date getExecutionTime(TimeZone meterTimeZone, Array executionTime) throws IOException {
        OctetString time = (OctetString) ((Structure) (executionTime.getDataType(0))).getDataType(0);
        OctetString date = (OctetString) ((Structure) (executionTime.getDataType(0))).getDataType(1);
        Date timeStamp = null;
        if (!isEmptyDateTime(time, date)) {
            timeStamp = new AXDRDateTime(date, time, meterTimeZone).getValue().getTime();
        }
        return timeStamp;
    }

    private boolean isEmptyDateTime(OctetString time, OctetString date) {
        return isEmptyTime(time) && isEmptyDate(date);
    }

    private boolean isEmptyDate(OctetString date) {
        return isAllCharacter(date.getOctetStr(), 0xFF);
    }

    private boolean isEmptyTime(OctetString time) {
        return isAllCharacter(time.getOctetStr(), 0x00) || isAllCharacter(time.getOctetStr(), 0xFF);
    }

    /**
     * Method that checks if the content of the OctetString is all the same characters
     */
    private boolean isAllCharacter(byte[] octetStr, int character) {
        for (byte b : octetStr) {
            if ((b & 0xFF) != character) {
                return false;
            }
        }
        return true;
    }
}
