/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.abstractnta.messages;

import com.elster.jupiter.calendar.Calendar;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import javax.xml.parsers.ParserConfigurationException;

public class AbstractDlmsMessaging {

    private final AbstractDlmsProtocol protocol;

    public AbstractDlmsMessaging(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    protected AbstractDlmsProtocol getProtocol() {
        return protocol;
    }

    protected String convertCodeTableToXML(Calendar messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }

}