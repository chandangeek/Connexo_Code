package com.energyict.genericprotocolimpl.elster.ctr.encryption;

import com.energyict.genericprotocolimpl.elster.ctr.frame.Frame;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;

/**
 * Copyrights EnergyICT
 * Date: 4-okt-2010
 * Time: 14:04:03
 */
public class CTREncryption {

    Cipher cipher;

    public CTREncryption() {
        try {
            cipher = Cipher.getInstance("AES/CMAC");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public Frame encryptFrame(Frame frame) {
        return frame;
    }

    public Frame decryptFrame(Frame frame) {
        return frame;
    }

    public static void main(String[] args) {
        CTREncryption ctrEncryption = new CTREncryption();
    }

}
