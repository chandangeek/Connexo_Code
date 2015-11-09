package com.energyict.protocolimplv2.nta.abstractnta.messages;

import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXmlParsing;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Copyrights EnergyICT
 * Date: 22/11/13
 * Time: 14:16
 * Author: khe
 */
public class AbstractDlmsMessaging {

    private final AbstractDlmsProtocol protocol;

    public AbstractDlmsMessaging(AbstractDlmsProtocol protocol) {
        this.protocol = protocol;
    }

    protected AbstractDlmsProtocol getProtocol() {
        return protocol;
    }

    protected String convertCodeTableToXML(Code messageAttribute) {
        try {
            return CodeTableXmlParsing.parseActivityCalendarAndSpecialDayTable(messageAttribute, 0, "0");
        } catch (ParserConfigurationException e) {
            throw new GeneralParseException(MessageSeeds.GENERAL_PARSE_ERROR, e);
        }
    }
}
