package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.attributes.RenewGMKSingleActionScheduleAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;

public class RenewGMKSingleActionScheduleIC extends AbstractCosemObject {

    public static final ObisCode OBIS_CODE = ObisCode.fromString("0.168.15.128.0.255");

    /**
     * Creates a new instance of AbstractCosemObject
     */
    public RenewGMKSingleActionScheduleIC(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public static final ObisCode getDefaultObisCode() {
        return OBIS_CODE;
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.SINGLE_ACTION_SCHEDULE.getClassId();
    }

    public AbstractDataType readAttribute(RenewGMKSingleActionScheduleAttributes attribute, AbstractDataType data) throws IOException {
        return readDataType(attribute, data.getClass());
    }

    public void writeAttribute(RenewGMKSingleActionScheduleAttributes attribute, AbstractDataType data) throws IOException {
        write(attribute, data);
    }

}