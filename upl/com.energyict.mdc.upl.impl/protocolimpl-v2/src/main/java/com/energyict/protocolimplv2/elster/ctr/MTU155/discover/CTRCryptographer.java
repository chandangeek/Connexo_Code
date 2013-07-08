package com.energyict.protocolimplv2.elster.ctr.MTU155.discover;

import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.mdc.protocol.inbound.crypto.MD5Seed;
import com.energyict.mdc.protocol.inbound.crypto.ServerCryptographer;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.encryption.CTREncryption;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRCipheringException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.SMSFrame;

/**
 * Provides an implementation for the {@link ServerCryptographer} specific for the CTR protocols (MTU155 and EK155)<br></br>
 * The cryptographer can be used to decrypt incoming SMSes, so we are able to extract the data out of it.
 *
 * @author sva
 * @since 26/06/13 - 16:05
 */
public class CTRCryptographer implements ServerCryptographer {

    private int usageCount = 0;

    public SMSFrame decryptSMS(MTU155Properties properties, byte[] encryptedMessage) {
        this.usageCount++;

        try {
            SMSFrame smsFrame = new SMSFrame().parse(encryptedMessage, 0);
            CTREncryption ctrEncryption = new CTREncryption(properties);
            return (SMSFrame) ctrEncryption.decryptFrame(smsFrame);
        } catch (CTRParsingException e) {
            throw MdcManager.getComServerExceptionFactory().createProtocolParseException(e);
        } catch (CTRCipheringException e) {
            throw MdcManager.getComServerExceptionFactory().createCipheringException(e);
        }
    }

    @Override
    public boolean wasUsed() {
        return this.usageCount > 0;
    }

    @Override
    public MD5Seed buildMD5Seed(DeviceIdentifier deviceIdentifier, String source) {
        return null;  //Not used in the CTR protocol
    }
}
