/*
 * PLCCTemplateObject.java
 *
 * Created on 3 december 2007, 13:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;


import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 *
 * @author kvds
 */
public class PLCCFTPServerId extends AbstractPLCCObject {

    private SMTPSetup smtpSetup=null;

    /** Creates a new instance of PLCCTemplateObject */
    public PLCCFTPServerId(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("0.0.25.5.0.255"), DLMSClassId.SMTP_SETUP.getClassId());
    }

    protected void doInvoke() throws IOException {
        smtpSetup = getCosemObjectFactory().getSMTPSetup(getId().getObisCode());
    }

    public SMTPSetup getSMTPSetup() throws IOException {
        return smtpSetup;
    }


    public void writeFtpServerId(com.energyict.protocolimpl.edf.messages.objects.FtpServerId ftpServerId) throws IOException {
        smtpSetup.writeServerPort(new Unsigned16(ftpServerId.getPortNumber()));
        smtpSetup.writeUserName(OctetString.fromString(ftpServerId.getUsername(),32));
        smtpSetup.writeLoginPassword(OctetString.fromString(ftpServerId.getPassword(),32));
        smtpSetup.writeServerAddress(OctetString.fromString(ftpServerId.getServerAddress(),64));
        smtpSetup.writeSenderAddress(OctetString.fromString(ftpServerId.getSenderAddress(),64));
    }


    public com.energyict.protocolimpl.edf.messages.objects.FtpServerId readFtpServerId() throws IOException {
        com.energyict.protocolimpl.edf.messages.objects.FtpServerId ftpServerId = new com.energyict.protocolimpl.edf.messages.objects.FtpServerId();
        ftpServerId.setPortNumber(smtpSetup.readServerPort().intValue());
        ftpServerId.setUsername(smtpSetup.readUserName().stringValue());
        ftpServerId.setPassword(smtpSetup.readLoginPassword().stringValue());
        ftpServerId.setServerAddress(smtpSetup.readServerAddress().stringValue());
        ftpServerId.setSenderAddress(smtpSetup.readSenderAddress().stringValue());
        return ftpServerId;
    }

}
