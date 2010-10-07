package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.CtrConnection;
import com.energyict.genericprotocolimpl.elster.ctr.MTU155Properties;
import com.energyict.genericprotocolimpl.elster.ctr.common.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;
import com.energyict.genericprotocolimpl.elster.ctr.frame.GPRSFrame;

import javax.crypto.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;

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
    public GPRSFrame sendFrameGetResponse(GPRSFrame frame) throws IllegalBlockSizeException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException, InvalidAlgorithmParameterException, CTRParsingException {

        //Encryption
        frame = (GPRSFrame) ctrEncryption.encryptFrame((Frame) frame);


        GPRSFrame gprsFrame = super.sendFrameGetResponse(frame);

        //Decrypt and return response
        gprsFrame = (GPRSFrame) ctrEncryption.decryptFrame((Frame) frame);

        return gprsFrame;
    }


}
