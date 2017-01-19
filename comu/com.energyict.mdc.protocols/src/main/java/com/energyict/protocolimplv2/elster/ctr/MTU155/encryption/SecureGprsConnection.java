package com.energyict.protocolimplv2.elster.ctr.MTU155.encryption;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.GprsConnection;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRCipheringException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.GPRSFrame;
import com.energyict.protocols.exception.ProtocolEncryptionException;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5-okt-2010
 * Time: 15:02:43
 */
public class SecureGprsConnection extends GprsConnection {

    private Logger logger = null;
    private CTREncryption ctrEncryption;
    private boolean debug;

    /**
     * 
     * @param comChannel
     * @param properties
     * @param logger
     */
    public SecureGprsConnection(ComChannel comChannel, MTU155Properties properties, Logger logger) {
        super(comChannel, properties);
        this.ctrEncryption = new CTREncryption(properties);
        this.debug = properties.isDebug();
        this.logger = logger;
    }

    /**
     *
     * @param comChannel
     * @param properties
     */
    public SecureGprsConnection(ComChannel comChannel, MTU155Properties properties) {
        this(comChannel, properties, null);
    }

    /**
     * Sends a given frame, and returns the meter response frame.
     * @param requestFrame: the given frame to send
     * @return the meter's response frame
     * @throws CTRConnectionException
     */
    @Override
    public GPRSFrame sendFrameGetResponse(GPRSFrame requestFrame)  {
        try {
            if (isDebug()) {
                getLogger().finest("TX[" + System.currentTimeMillis() +  "] " + ProtocolTools.getHexStringFromBytes(requestFrame.getBytes()));
            }
            GPRSFrame encryptedFrame = (GPRSFrame) ctrEncryption.encryptFrame(requestFrame);
            GPRSFrame responseFrame = super.sendFrameGetResponse(encryptedFrame);
            GPRSFrame unencryptedResponseFrame = (GPRSFrame) ctrEncryption.decryptFrame(responseFrame);
            if (isDebug()) {
                getLogger().finest("RX[" + System.currentTimeMillis() +  "] " + ProtocolTools.getHexStringFromBytes(unencryptedResponseFrame.getBytes()));
            }
            return unencryptedResponseFrame;
        } catch (CTRCipheringException e) {
            throw new ProtocolEncryptionException(e, MessageSeeds.ENCRYPTION_ERROR);
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * 
     * @return
     */
    private Logger getLogger() {
        if (this.logger == null) {
            this.logger = Logger.getLogger(getClass().getCanonicalName());
        }
        return this.logger;
    }

    @Override
    public CTREncryption getCTREncryption() {
        return ctrEncryption;
    }
}
