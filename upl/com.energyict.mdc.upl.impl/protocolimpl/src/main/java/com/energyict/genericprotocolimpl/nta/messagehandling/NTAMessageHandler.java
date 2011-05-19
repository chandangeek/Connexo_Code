package com.energyict.genericprotocolimpl.nta.messagehandling;

import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
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
        } else if(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY.equals(qName)) {
            setType(RtuMessageConstant.NTA_AEE_CHANGE_DATATRANSPORT_ENCRYPTION_KEY);
        } else {
            super.startElement(uri, lName, qName, attrbs);
        }

    }
}