package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.CtrConnection;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CtrCipheringException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyrights EnergyICT
 * Date: 5-okt-2010
 * Time: 15:02:43
 */
public class SecureCtrConnection extends CtrConnection {

    private CTREncryption ctrEncryption;


    public SecureCtrConnection(InputStream in, OutputStream out, MTU155Properties properties) {
        super(in, out, properties);
        ctrEncryption = new CTREncryption(properties.getKeyC(), properties.getKeyT(), properties.getKeyF());
    }

    @Override
    public GPRSFrame sendFrameGetResponse(GPRSFrame frame) throws CTRConnectionException {
        try {
            frame = (GPRSFrame) ctrEncryption.encryptFrame((Frame) frame);
            GPRSFrame gprsFrame = super.sendFrameGetResponse(frame);
            gprsFrame = (GPRSFrame) ctrEncryption.decryptFrame((Frame) frame);
            return gprsFrame;
        } catch (CtrCipheringException e) {
            throw new CTRConnectionException("An error occured in the secure connection!", e);
        }
    }


}
