package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.attributes.NbiotOrphanStateAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9/27/12
 * Time: 10:51 AM
 */
public class NbiotOrphanState extends AbstractCosemObject {


    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.0.94.39.10.255");

    /**
     * Creates a new instance of NBIOT Orphan State
     */
    public NbiotOrphanState(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.REGISTER_MONITOR.getClassId();
    }

    public AbstractDataType readThresholds() throws IOException {
        return readDataType(NbiotOrphanStateAttributes.THRESHOLDS);
    }

    public void writeThresholds(int numberOfDays) throws IOException {
        Array thresholds = new Array();
        thresholds.addDataType(new Unsigned16(numberOfDays));
        write(NbiotOrphanStateAttributes.THRESHOLDS, thresholds.getBEREncodedByteArray());
    }
}