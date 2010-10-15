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

/**
 * Copyrights EnergyICT
 * Date: 5-okt-2010
 * Time: 15:02:43
 */
public class SecureGprsConnection extends GprsConnection {

    private CTREncryption ctrEncryption;
    private boolean debug;

    public SecureGprsConnection(InputStream in, OutputStream out, MTU155Properties properties) {
        super(in, out, properties);
        ctrEncryption = new CTREncryption(properties.getKeyC(), properties.getKeyT(), properties.getKeyF());
        debug = false;
    }

    @Override
    public GPRSFrame sendFrameGetResponse(GPRSFrame requestFrame) throws CTRConnectionException {
        try {
            if (isDebug()) {
                System.out.println("TX[" + System.currentTimeMillis() +  "] " + ProtocolTools.getHexStringFromBytes(requestFrame.getBytes()));
            }
            GPRSFrame encryptedFrame = (GPRSFrame) ctrEncryption.encryptFrame(requestFrame);
            GPRSFrame responseFrame = super.sendFrameGetResponse(encryptedFrame);
            GPRSFrame unencryptedResponseFrame = (GPRSFrame) ctrEncryption.decryptFrame((Frame) responseFrame);
            if (isDebug()) {
                System.out.println("RX[" + System.currentTimeMillis() +  "] " + ProtocolTools.getHexStringFromBytes(unencryptedResponseFrame.getBytes()));
            }
            return unencryptedResponseFrame;
        } catch (CtrCipheringException e) {
            throw new CTRConnectionException("An error occured in the secure connection!", e);
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
