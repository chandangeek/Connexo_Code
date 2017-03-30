/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AbstractDataType;

import java.io.IOException;
import java.util.Calendar;

public class ComposedGenericRead extends GenericRead {

    private final AbstractDataType data;
    private final DLMSAttribute attribute;

    public ComposedGenericRead(AbstractDataType data, DLMSAttribute attribute, ProtocolLink protocolLink) {
        super(protocolLink, new ObjectReference(attribute.getObisCode().getLN()), attribute.getAttribute());
        this.data = data;
        this.attribute = attribute;
    }

    @Override
    protected byte[] getResponseData(int attribute, Calendar from, Calendar to) throws IOException {
        return data.getBEREncodedByteArray();
    }

}
