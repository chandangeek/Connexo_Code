package com.energyict.protocolimplv2.elster.ctr.MTU155.encryption;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.GprsConnection;
import com.energyict.protocolimplv2.elster.ctr.MTU155.MTU155Properties;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRCipheringException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.Frame;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.GPRSFrame;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5-okt-2010
 * Time: 15:02:43
 */
public class SecureGprsConnection extends GprsConnection {

    private Logger logger = null;
    private CTREncryption ctrEncryption;

    /**
     * 
     * @param in
     * @param out
     * @param properties
     * @param logger
     */
    public SecureGprsConnection(InputStream in, OutputStream out, MTU155Properties properties, Logger logger) {
        super(in, out, properties);
        this.ctrEncryption = new CTREncryption(properties);
        this.logger = logger;
    }

    /**
     *
     * @param in
     * @param out
     * @param properties
     */
    public SecureGprsConnection(InputStream in, OutputStream out, MTU155Properties properties) {
        this(in, out, properties, null);
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
            GPRSFrame unencryptedResponseFrame = (GPRSFrame) ctrEncryption.decryptFrame((Frame) responseFrame);
            if (isDebug()) {
                getLogger().finest("RX[" + System.currentTimeMillis() +  "] " + ProtocolTools.getHexStringFromBytes(unencryptedResponseFrame.getBytes()));
            }
            return unencryptedResponseFrame;
        } catch (CTRCipheringException e) {
            throw MdcManager.getComServerExceptionFactory().createCipheringException(e);
        }
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
