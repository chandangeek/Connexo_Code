/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.soap;

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.UnmarshalException;
import java.util.Objects;

public class EnabledSchemaValidationInterceptor extends AbstractPhaseInterceptor<Message> {
    public EnabledSchemaValidationInterceptor() {
        super(Phase.UNMARSHAL);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        message.put("schema-validation-enabled", true);
    }

    @Override
    public void handleFault(Message message) {
        Fault fault = (Fault) message.getContent(Exception.class);
        if (isNotSchemaCompliantXml(fault.getCause(), fault.getMessage())) {
            fault.setStatusCode(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private boolean isNotSchemaCompliantXml(Throwable faultCause, String faultMessage) {
        if (faultCause instanceof UnmarshalException
                || Objects.nonNull(faultMessage) && faultMessage.contains("Unexpected wrapper element")) {
            return true;
        }
        return false;
    }
}