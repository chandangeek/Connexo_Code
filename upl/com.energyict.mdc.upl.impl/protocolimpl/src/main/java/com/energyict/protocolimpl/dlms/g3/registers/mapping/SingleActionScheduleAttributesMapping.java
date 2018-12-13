package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.SchedulerMapping;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by H165680 on 6/16/2017.
 */
public class SingleActionScheduleAttributesMapping extends RegisterMapping {
    private static final int MIN_ATTR = 2;
    private static final int MAX_ATTR = 4;
    private static final Map<Integer, String> modes = new HashMap<Integer, String>();

    static {
        modes.put(1, "Mode 1: Size of \"execution time\" array = 1 and wildcard in the date is allowed.");
        modes.put(2, "Mode 2: Size of \"execution time\" array = n, all time values are the same and wildcards in the date are not allowed.");
        modes.put(3, "Mode 3: Size of \"execution time\" array = n, all time values are the same and wildcards in the date are allowed.");
        modes.put(4, "Mode 4: Size of \"execution time\" array = n, all time values may be different and wildcards in the date are not allowed.");
        modes.put(5, "Mode 5: Size of \"execution time\" array = n, all time values may be different and wildcards in the date are allowed.");
    }

    private final TimeZone timeZone;

    public SingleActionScheduleAttributesMapping(TimeZone timeZone, CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
        this.timeZone = timeZone;
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return SchedulerMapping.SCHEDULER_BASE_OBISCODE.equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getB() >= MIN_ATTR) &&
                (obisCode.getB() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final SingleActionSchedule singleActionSchedule = getCosemObjectFactory().getSingleActionSchedule(obisCode);
        return parse(obisCode, readAttribute(obisCode, singleActionSchedule));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, SingleActionSchedule singleActionSchedule) throws IOException {
        switch (obisCode.getB()) {
            case 2:
                return singleActionSchedule.readExecutedScript();
            case 3:
                return singleActionSchedule.readType();
            case 4:
                return singleActionSchedule.getExecutionTime();
            default:
                throw new NoSuchRegisterException("Single Action Scheduler attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getB()) {
            case 2:
                Structure executedScript = abstractDataType.getStructure();
                return new RegisterValue(obisCode, "Executed script: " + ObisCode.fromByteArray(executedScript.getDataType(0).getOctetString().getContentByteArray())+ ", script number: "+executedScript.getDataType(1).getUnsigned16().getValue());
            case 3:
                int mode = ((TypeEnum) abstractDataType).getValue();
                String modeDescription = modes.get(mode);
                return new RegisterValue(obisCode, modeDescription == null ? ("Unknown type: " + mode) : modeDescription);
            case 4:
                Date captureTime = getExecutionTime(timeZone, abstractDataType.getArray());
                String executionTimeDescription = "Execution time: " + (captureTime == null ? "Disabled" : captureTime.toString());
                return new RegisterValue(obisCode, executionTimeDescription);
            default:
                throw new NoSuchRegisterException("Single Action Scheduler attribute [" + obisCode.getB() + "] not supported!");

        }
    }

    public Date getExecutionTime(TimeZone meterTimeZone, Array executionTime) throws IOException {
        Date timeStamp = null;
        if (executionTime.getAllDataTypes().size() > 0) {
            OctetString time = (OctetString) ((Structure) (executionTime.getDataType(0))).getDataType(0);
            OctetString date = (OctetString) ((Structure) (executionTime.getDataType(0))).getDataType(1);
            if (!isEmptyDateTime(time, date)) {
                timeStamp = new AXDRDateTime(date, time, meterTimeZone).getValue().getTime();
            }
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
