package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exception.DataParseException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.CTREncryption;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRCipheringException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.SMSFrame;

/**
 * Provides an implementation specific for the CTR protocols (MTU155 and EK155)<br></br>
 * The cryptographer can be used to decrypt incoming SMSes, so we are able to extract the data out of it.
 *
 * @author sva
 * @since 26/06/13 - 16:05
 */
public class CTRCryptographer {

    private int usageCount = 0;

    public SMSFrame decryptSMS(MTU155Properties properties, byte[] encryptedMessage) {
        this.usageCount++;

        try {
            SMSFrame smsFrame = new SMSFrame().parse(encryptedMessage, 0);
            CTREncryption ctrEncryption = new CTREncryption(properties);
            return (SMSFrame) ctrEncryption.decryptFrame(smsFrame);
        } catch (CTRParsingException e) {
            throw DataParseException.ioException(e);
        } catch (CTRCipheringException e) {
            throw ConnectionCommunicationException.cipheringException(e);
        }
    }

    public boolean wasUsed() {
        return this.usageCount > 0;
    }
}