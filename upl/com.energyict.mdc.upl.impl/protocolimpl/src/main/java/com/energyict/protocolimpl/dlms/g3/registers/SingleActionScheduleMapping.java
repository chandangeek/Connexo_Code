package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 10:27
 */
public class SingleActionScheduleMapping extends G3Mapping {

    public SingleActionScheduleMapping(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        SingleActionSchedule singleActionSchedule = as330D.getSession().getCosemObjectFactory().getSingleActionSchedule(getObisCode());
        Array executionTime = singleActionSchedule.getExecutionTime();
        OctetString time = (OctetString) ((Structure) (executionTime.getDataType(0))).getDataType(0);
        OctetString date = (OctetString) ((Structure) (executionTime.getDataType(0))).getDataType(1);
        Date timeStamp = null;
        if (!isEmptyDateTime(time, date)) {
            timeStamp = new AXDRDateTime(date, time, as330D.getSession().getTimeZone()).getValue().getTime();
        }
        return new RegisterValue(getObisCode(), "Execution time: " + (timeStamp == null ? "default" : timeStamp.toString()));
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
