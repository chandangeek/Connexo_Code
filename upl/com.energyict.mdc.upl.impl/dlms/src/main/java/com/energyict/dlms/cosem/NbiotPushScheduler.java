package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.DateTimeOctetString;
import com.energyict.dlms.cosem.attributes.NbiotPushSchedulerAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class NbiotPushScheduler extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.128.0.12.255");

    /**
     * Creates a new instance of PushSetupConfig
     */
    public NbiotPushScheduler(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.SINGLE_ACTION_SCHEDULE.getClassId();
    }

    public void writeExecutionTime(Calendar[] executionTimes) throws IOException {
        Array timesArray = new Array();
        for(Calendar execTime: executionTimes) {
            timesArray.addDataType(new DateTimeOctetString(execTime));
        }
        write(NbiotPushSchedulerAttributes.EXECUTION_TIME, timesArray.getBEREncodedByteArray());
    }
}