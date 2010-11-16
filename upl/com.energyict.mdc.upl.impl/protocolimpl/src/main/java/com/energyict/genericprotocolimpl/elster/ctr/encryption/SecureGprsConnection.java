package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.GprsConnection;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;
import com.energyict.protocolimpl.utils.ProtocolTools;

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
    private boolean debug;

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
        this.debug = properties.isDebug();
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

    @Override
    public GPRSFrame sendFrameGetResponse(GPRSFrame requestFrame) throws CTRConnectionException {
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
        } catch (CtrCipheringException e) {
            throw new CTRConnectionException("An error occurred in the secure connection!", e);
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

}
