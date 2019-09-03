package com.energyict.dlms.cosem;

import com.energyict.dlms.ProtocolLink;

import java.io.IOException;

public class TableRead extends AbstractCosemObject {

    public TableRead(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.UTILITY_TABLES.getClassId();
    }

    public byte[] getBuffer(int fromOffset, int length) throws IOException {
        return getLNResponseData(4, fromOffset, length);
    }
}
