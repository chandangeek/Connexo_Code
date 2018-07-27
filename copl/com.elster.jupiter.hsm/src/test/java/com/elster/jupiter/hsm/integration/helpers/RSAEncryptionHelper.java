package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;

import org.bouncycastle.util.encoders.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * This is not a proper test. This class was created during integration test of HSM importer while nobody could provide a valid import file
 * and therefore we had to encrypt our own keys using this helper. Of course one needs to provide proper public key to work with this impl.
 */
public class RSAEncryptionHelper {

    private static final String PUBLIC_KEY_HEX = "30820122300D06092A864886F70D01010105000382010F003082010A0282010100927E102F1B8852C10F5F0BC043120013251E88AB9257AB8C11DFD47D20B6613A36E3BD1E3CDC84ABED9CF96F41B80469483D9079365D3D93180763254CC0DAC34F68B2CF6DCC3CE7096F797F622943983849F0F0E7802C258DAEEF842691170A7BC974DFC30D4D90DA135C843E5FF3B5572A6FBB3E597E35A330821CEE651423548E216389CFD076600142CBD724D1263A0BD4013971B8ECD73847428DA0C32BA1635B723AFE7C86E38DCEEA764B0B865F20650CD71548395A96851B32FFFC53D51A6B1B3B4CBC21917FA3CC355069D154DCAF0DBA1D1A8A5098DC3625F4F019DF94BD751F11120A3CCA704CDC7759A3E29BF6A4E0C6D8FA9D815ACE7590FEBD0203010001";


    public Message encrypt(Message plainTxt, AsymmetricAlgorithm alg) throws
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException, InvalidKeySpecException {

        Cipher c = Cipher.getInstance(alg.getAlgorithm());

        byte[] publicKeyBytes = Hex.decode(PUBLIC_KEY_HEX);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory kf = KeyFactory.getInstance(alg.getAlgorithm());
        PublicKey publicKey = kf.generatePublic(publicKeySpec);
        c.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] bytes = c.doFinal(plainTxt.getBytes());

        return new Message(bytes);
    }

}
