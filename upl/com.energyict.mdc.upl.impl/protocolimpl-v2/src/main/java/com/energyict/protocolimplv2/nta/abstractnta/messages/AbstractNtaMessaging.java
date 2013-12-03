package com.energyict.protocolimplv2.nta.abstractnta.messages;

import com.energyict.mdw.core.Code;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaProtocol;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 14:16
 * Author: khe
 */
public class AbstractNtaMessaging {

    private final AbstractNtaProtocol protocol;

    public AbstractNtaMessaging(AbstractNtaProtocol protocol) {
        this.protocol = protocol;
    }

    protected AbstractNtaProtocol getProtocol() {
        return protocol;
    }

    protected String convertCodeTableToXML(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw MdcManager.getComServerExceptionFactory().createGeneralParseException(e);
        }
    }
}
