package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.GprsConnection;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.exception.*;
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

    public static void main(String[] args) throws CTRParsingException, CtrCipheringException {
        String keyC = "32323232323232323232323232323232";
        byte[] decrypt = ProtocolTools.getBytesFromHexString("0A000000668749C91E0BB8E03A571504DAAA99335E3E8732B5CA85C255BB170A5BB746D1148B15C46CFB077609873BF142E74BC45CDFFBE0C740E3E16021F5DF81BFAD76C804C845E554677E1612BDB1E633FE607D9A26403258A4FEC824AE65AA5F28315B993875E23DB92754A4352C92E906BD2C0FA58B9A7A376C234316EB081BF0FC37B63C99F78EA661CB0D", "");
        CTREncryption encr = new CTREncryption(keyC, keyC, keyC);
        Frame frame = encr.decryptFrame(new GPRSFrame().parse(decrypt, 0));
        System.out.println(ProtocolTools.getHexStringFromBytes(frame.getBytes()));
    }
}
