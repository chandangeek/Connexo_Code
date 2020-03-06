package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.attributes.NbiotPushSchedulerAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;
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
            Structure dataTimeStruct = new Structure();
            AXDRDateTime dataTime = new AXDRDateTime(execTime);
            dataTimeStruct.addDataType(dataTime.getCosemTime().getOctetString());
            dataTimeStruct.addDataType(dataTime.getCosemDate().getOctetString());
            timesArray.addDataType(dataTimeStruct);
        }
        write(NbiotPushSchedulerAttributes.EXECUTION_TIME, timesArray.getBEREncodedByteArray());
    }
}