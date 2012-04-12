package com.energyict.protocolimpl.ansi.c12;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.engines.AESEngine;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.engines.AESFastEngine;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.modes.EAXBlockCipher;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.modes.EAXPBlockCipher;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.params.AEADParameters;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.crypto.params.KeyParameter;
import com.energyict.protocolimpl.ansi.c12.EAXPrime.util.encoders.Hex;

public class C1222Encryption {

    public static long expectedMacValue = 0xE68017cd; // 0xCD1780E6; 3440869606

    public static byte canonifiedCleartext[] = {
            (byte) 0xa2, (byte) 0x0f, (byte) 0x06, (byte) 0x0d, (byte) 0x2b, (byte) 0x06, (byte) 0x01, (byte) 0x04,
            (byte) 0x01, (byte) 0x82, (byte) 0x85, (byte) 0x63, (byte) 0x8e, (byte) 0x7f, (byte) 0x81, (byte) 0xb2,
            (byte) 0x7b, (byte) 0xa8, (byte) 0x06, (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x34, (byte) 0x56,
            (byte) 0x78, (byte) 0xac, (byte) 0x0f, (byte) 0xa2, (byte) 0x0d, (byte) 0xa0, (byte) 0x0b, (byte) 0xa1,
            (byte) 0x09, (byte) 0x80, (byte) 0x01, (byte) 0x00, (byte) 0x81, (byte) 0x04, (byte) 0x4b, (byte) 0x78,
            (byte) 0x6a, (byte) 0x91, (byte) 0xbe, (byte) 0x19, (byte) 0x28, (byte) 0x17, (byte) 0x81, (byte) 0x15,
            (byte) 0x84, (byte) 0xa6, (byte) 0x0e, (byte) 0x06, (byte) 0x0c, (byte) 0x2b, (byte) 0x06, (byte) 0x01,
            (byte) 0x04, (byte) 0x01, (byte) 0x82, (byte) 0x85, (byte) 0x63, (byte) 0x8e, (byte) 0x7f, (byte) 0x4d,
            (byte) 0x01, (byte) 0x00, (byte) 0x4b, (byte) 0x78, (byte) 0x6a, (byte) 0x91,

            (byte) 0x0f, (byte) 0x50,
            (byte) 0x00, (byte) 0x00, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20,
            (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x00, (byte) 0x3c};

    public C1222Encryption() {
    }

    public static void authenticate() {
        EAXPBlockCipher eaxBlockCipher = null;
        KeyParameter keyParameter = null;
        AEADParameters aeadParameters = null;
        byte[] mac = null;
        byte[] result = new byte[canonifiedCleartext.length];
        int macAsInt = 0;
        int macSize = 32;
        int len = 0;
        String securityKey = "F268736453E6BC5FD39F6D201AAA2C14";
        String nonce = "0";

        try {
            eaxBlockCipher = new EAXPBlockCipher(new AESFastEngine());
            keyParameter = new KeyParameter(Hex.decode(securityKey));
            aeadParameters = new AEADParameters(keyParameter, macSize, nonce.getBytes(), canonifiedCleartext);

            //C1222Layer.longToHex(expectedMacValue)
            eaxBlockCipher.init(true, aeadParameters);
            len = eaxBlockCipher.processBytes("".getBytes(), 0, 0, result, 0);
            len += eaxBlockCipher.doFinal(result, len);

            mac = eaxBlockCipher.getMac();

            if (mac == null || mac.length == 0) {
                System.err.println("mac is null or zero length");
            } else {
                macAsInt = ProtocolUtils.getInt(mac);
                System.err.println("Expected = " + expectedMacValue + ";  Actual=" + macAsInt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("Test");
    }

    public static void encrypt() {
        int macSize = 4;
        String securityKey = "F268736453E6BC5FD39F6D201AAA2C14";
        String nonce = "";

        EAXBlockCipher eaxBlockCipher = new EAXBlockCipher(new AESEngine());
        KeyParameter keyParameter = new KeyParameter(securityKey.getBytes());
        AEADParameters aeadParameters = new AEADParameters(keyParameter, macSize, nonce.getBytes(), canonifiedCleartext);

        eaxBlockCipher.init(true, aeadParameters);

    }

    public static void main(String[] args) {
        authenticate();
        //encrypt();
    }
}