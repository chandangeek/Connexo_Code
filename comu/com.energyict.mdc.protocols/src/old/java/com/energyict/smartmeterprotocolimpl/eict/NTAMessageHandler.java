/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict;

import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles the parsing of the NTA changeKey implementation. The content of the XML-tags has changed as from 19-05-2011 and because allot of protocols used them
 * we need to define them directly for the NTA protocols
 */
public class NTAMessageHandler extends MessageHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void startElement(final String uri, final String lName, final String qName, final Attributes attrbs) throws SAXException {

        if (RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY.equals(qName)) {
            setType(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_AUTHENTICATION_KEY);
            handleChangeAuthenticationKey(attrbs);
        } else if (RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY.equals(qName)) {
            setType(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY);
            handleChangeEncryptionKey(attrbs);
        } else if (RtuMessageConstant.RESET_ALARM_REGISTER.equals(qName)) {
            setType(RtuMessageConstant.RESET_ALARM_REGISTER);
        } else if (RtuMessageConstant.RESET_ERROR_REGISTER.equals(qName)) {
            setType(RtuMessageConstant.RESET_ERROR_REGISTER);
        } else if (RtuMessageConstant.WEBSERVER_DISABLE.equals(qName)) {
            setType(RtuMessageConstant.WEBSERVER_DISABLE);
        } else if (RtuMessageConstant.WEBSERVER_ENABLE.equals(qName)) {
            setType(RtuMessageConstant.WEBSERVER_ENABLE);
        } else if (RtuMessageConstant.REBOOT.equals(qName)) {
            setType(RtuMessageConstant.REBOOT);
        } else if (RtuMessageConstant.DEBUG_LOGBOOK.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.DEBUG_LOGBOOK);
            handleLogbookParameters(attrbs);
        } else if (RtuMessageConstant.ELSTER_SPECIFIC_LOGBOOK.equalsIgnoreCase(qName)) {
            setType(RtuMessageConstant.ELSTER_SPECIFIC_LOGBOOK);
            handleLogbookParameters(attrbs);
        } else {
            super.startElement(uri, lName, qName, attrbs);
        }

    }
}