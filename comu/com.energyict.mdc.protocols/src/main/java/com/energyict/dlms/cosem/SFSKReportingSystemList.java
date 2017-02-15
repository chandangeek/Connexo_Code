/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.RegisterReadable;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.attributes.SFSKReportingSystemListAttribute;

import java.io.IOException;

/**
 * @author jme
 */
public class SFSKReportingSystemList extends AbstractCosemObject implements RegisterReadable {

    private static final byte[] LN = ObisCode.fromString("0.0.26.6.0.255").getLN();

    /**
     * @return
     */
    public static ObisCode getDefaultObisCode() {
        return ObisCode.fromByteArray(LN);
    }

    @Override
    protected int getClassId() {
        return DLMSClassId.S_FSK_REPORTING_SYSTEM_LIST.getClassId();
    }

    /**
     * @param protocolLink
     * @param objectReference
     */
    public SFSKReportingSystemList(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    /**
     * Get the logicalname of the object. Identifies the object instance.
     *
     * @return
     */
    public OctetString getLogicalName() {
        try {
            return new OctetString(getResponseData(SFSKReportingSystemListAttribute.LOGICAL_NAME));
        } catch (IOException e) {
            return null;
        }
    }

    public Array getReportingSystemList() {
        try {
            return new Array(getResponseData(SFSKReportingSystemListAttribute.REPORTING_SYSTEM_LIST), 0, 0);
        } catch (IOException e) {
            return null;
        }
    }

    public void setReportingSystemList(Array reportingSystemList) throws IOException {
        write(SFSKReportingSystemListAttribute.REPORTING_SYSTEM_LIST, reportingSystemList.getBEREncodedByteArray());
    }

    @Override
    public String toString() {
        final String crlf = "\r\n";

        Array reportingSystemList = getReportingSystemList();

        StringBuffer sb = new StringBuffer();
        sb.append("SFSKReportingSystemList").append(crlf);
        sb.append(" > reportingSystemList = ").append(reportingSystemList != null ? reportingSystemList.toString() : null).append(crlf);
        return sb.toString();
    }

    public RegisterValue asRegisterValue() {
        return new RegisterValue(getDefaultObisCode(), toString());
    }

    public RegisterValue asRegisterValue(int attributeNumber) {
        SFSKReportingSystemListAttribute attribute = SFSKReportingSystemListAttribute.findByAttributeNumber(attributeNumber);
        if (attribute != null) {
            switch (attribute) {
                case LOGICAL_NAME:
                    OctetString ln = getLogicalName();
                    return new RegisterValue(getDefaultObisCode(), ln != null ? ObisCode.fromByteArray(ln.getContentBytes()).toString() : "null");
                case REPORTING_SYSTEM_LIST:
                    Array reportingSystemList = getReportingSystemList();
                    return new RegisterValue(getDefaultObisCode(), reportingSystemList != null ? reportingSystemList.toString() : "null");
            }
        }
        return null;
    }
}