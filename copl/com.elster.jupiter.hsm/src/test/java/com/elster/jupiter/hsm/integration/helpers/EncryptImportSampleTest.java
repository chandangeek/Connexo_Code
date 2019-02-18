package com.elster.jupiter.hsm.integration.helpers;

import com.elster.jupiter.hsm.integration.helpers.keys.AsymetricKey;
import com.elster.jupiter.hsm.integration.helpers.keys.Encoder;
import com.elster.jupiter.hsm.model.Message;
import com.elster.jupiter.hsm.model.krypto.AsymmetricAlgorithm;
import com.elster.jupiter.hsm.model.krypto.SymmetricAlgorithm;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This is not a proper test. This class was created during integration test of HSM importer while nobody could provide a valid import file
 * and therefore we had to encrypt our own keys using this helper.
 */
@Ignore
public class EncryptImportSampleTest {

    private static final AsymetricKey KEY = new AsymetricKey("30820122300D06092A864886F70D01010105000382010F003082010A02820101008369BB10E598D567D1C90A96418CEE71A6D80150F4468C2E8674DD78725024DD091C034BFCDB422216F5F54DB6A0492B034419D3C02E5B68B10B3F11DAF251D2FF27F40D3877B57282B46B5CE035F480B910D9A9D2D4478D93DA5E471699AAC4BB474B3C9FBBC7B9031CBAD2791E57E2BFA9032D2D415D6F602AD0841A9631D9478B5A5E1786DCEC4EEB47736A33F61124070466369DCD556C3C37CD1E88E9D453B7270519D4FF4513F755D5F168D4FC6C792532C12DB65B0AD0BF3EA8976E968B849BC43FF9B1638837F67DAB33121A90B89CAEE869454757AA948C940A21E21732DC2BF2B0E85671C2877A8F23B6F6478FFFB0B70E246A30885971B1A03E810203010001", "308204BD020100300D06092A864886F70D0101010500048204A7308204A302010002820101008369BB10E598D567D1C90A96418CEE71A6D80150F4468C2E8674DD78725024DD091C034BFCDB422216F5F54DB6A0492B034419D3C02E5B68B10B3F11DAF251D2FF27F40D3877B57282B46B5CE035F480B910D9A9D2D4478D93DA5E471699AAC4BB474B3C9FBBC7B9031CBAD2791E57E2BFA9032D2D415D6F602AD0841A9631D9478B5A5E1786DCEC4EEB47736A33F61124070466369DCD556C3C37CD1E88E9D453B7270519D4FF4513F755D5F168D4FC6C792532C12DB65B0AD0BF3EA8976E968B849BC43FF9B1638837F67DAB33121A90B89CAEE869454757AA948C940A21E21732DC2BF2B0E85671C2877A8F23B6F6478FFFB0B70E246A30885971B1A03E81020301000102820100714EED18F89EFB02F583DF39077438F48DE399DEEF8114C5D8F15334D37BFF0D0719BC4DFCCC210DBBB67FB86E4B6E4B4419A81B488DB00B68FC457963CBA479C5BFF152A4E9B2EDF57D1345959D7020BD71A5FC23E91D79198F23D32FCD77CD019DCB658E651F248B666E3FA467616805D1F9072F0A39421B2D4F75586740BAD35376772AB714DCC637AB2841D2BE69C472E4C988CE2E350C43D3FFF7F7EAFA1CCE8C21A55F32EA1790F0936F99A106120CAC894C184B8BF1FE6D20F1838BCCB6E647DD0FBA646896C9B7CD85692D577B8A7698D394D00687F1C2E86BF6BFF450B7A93D65008D81105CEA2AAA88A44F6D4024EC1161E074DC63A37BA68A95F102818100D9D916800FA9EA2E1B2C04831C95C4CDF9C8F806139FF9B29752643DBB80B056B3A88140A03252611860A0F431104CAA825903622784B2F72962A6F0242D97B97368722EEDF843B5DF04CC325780B7ABE08E36587CF398372AB47386E70DA4CFC5837F6652ADCB47236CCC15860D139B27ECBF7A26DB1B16C2A07E20EEC42F07028181009A6D734766A09FCD6458B4E76FFB0240D9C33764C4BE68124443D576B94EEBAFC939D38F27C70FCA798DEF697AB363D7D5DA7AE892678AE1DA6363A3D20B0123B809082BA81EDFFE085CD520A0A9F35AF334AA3485E429DE248D441D00C3D2D36F5E2B836F90A5D9B1D483F6A31F73479E32F17052D990277FD7F25E7847BC3702818100A5A16C7E096AC2E6333A9063AF441FD20B6C4547397C9438B8DCA7E257C14C515F5F6A865C466663F448E39746068283D17F241768BC77E57BCDC9E7235A96D5256DF002C663CFF7638D1E43D84BC15A28ED775C68043D63145106D536AF24F3E3D44AF4DD3FCE225448D0123D4D8BEE97B8650CD6A6183E81D3CC91F577ABD90281804202B31A3C4640C8DD3205F2402DBDA2D6F2D984DB8CC093BE5678B2CD376D0BB12A64C276B062919C300DBCBAD45FC36D087D2D5917A588317FF6A19A3156055CAF8FAC89AAD8F88FBE8EE3E9897C1ACE871E9261014CDC4627948C093DABAAF19A0163A796DF2E1513D06CE7019497728A38265963E278DFEF010D5CC0DD3502818076DC171ACB53DC4204A811723269B0ECBBA351D2F4DE7875039C9DCDA586A2C555311711A362E26AB80953FA66834ABA614485539BB9328F59FEF13EAF6360F5B903F4B76F02B7CA41093D968EC2208957701AC514D1105F05638527E475B101619744B633BF74E647D1E6D551BAC9D5F1B14487B099D9FBA6C250D5790F6F4E", Encoder.HEX);

    public static final String ENCRYPT_OUT = "encrypt.out";


    @Before
    public void setUp() throws IOException {
        File f = new File(ENCRYPT_OUT);
        if (f.exists()) {
            f.delete();
        }
        f.createNewFile();
    }

    @Test
    public void generateImportSample() throws
            InvalidKeyException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            IOException,
            InvalidAlgorithmParameterException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeySpecException {

        Message wrapperKey = new Message("PasswordPasswordPasswordPassword");
        Message initVector = new Message("0123456789ABCDEF");
        Message deviceKey = new Message("Password");


        // including IV and encrypted key
        String encryptedDeviceKey = encryptDeviceKey(deviceKey, initVector, wrapperKey);
        String encryptedWrapperKey = encryptWrapperKey(wrapperKey);

        writeToFile(encryptedWrapperKey, encryptedDeviceKey);
    }

    private String encryptWrapperKey(Message encryptionPassword) throws
            NoSuchPaddingException,
            NoSuchAlgorithmException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidKeyException,
            InvalidKeySpecException {
        RSAEncryptionHelper rsaEncryptionHelper = new RSAEncryptionHelper();
        Message encrypt = rsaEncryptionHelper.encrypt(encryptionPassword, AsymmetricAlgorithm.RSA_15, KEY);
        return Base64.getEncoder().encodeToString(encrypt.getBytes());

    }

    private String encryptDeviceKey(Message message, Message initVector, Message encryptionKey ) throws
            IOException,
            InvalidKeyException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidAlgorithmParameterException {
        AESEncryptionHelper aesEncryptionHelper = new AESEncryptionHelper();
        Message encryptedPassword = aesEncryptionHelper.encrypt(message, encryptionKey, initVector, SymmetricAlgorithm.AES_256_CBC);
        String ivAndEncrypted = getFullDeviceKey(initVector, encryptedPassword);

        return ivAndEncrypted;
    }

    private synchronized void writeToFile(String wrappedKey, String fullDeviceKey) throws FileNotFoundException {
        File f = new File(ENCRYPT_OUT);
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f), Charset.defaultCharset()))) {
            String wrapperKeyTxt = "Wrapper key:" + wrappedKey;
            System.out.println(wrapperKeyTxt);
            pw.println(wrapperKeyTxt);
            String deviceKeyTxt = "Device Password:" + fullDeviceKey;
            System.out.println(deviceKeyTxt);
            pw.println(deviceKeyTxt);
        }
    }

    private String getFullDeviceKey(Message initVector, Message encryptedPassword) {
        if (!initVector.getCharSet().equals(encryptedPassword.getCharSet()))  {
            throw new RuntimeException("Incompatible charset");
        }
        byte[] iv = initVector.getBytes();
        byte[] epb = encryptedPassword.getBytes();
        byte[] allBytes = new byte[iv.length + epb.length];
        System.arraycopy(iv, 0, allBytes, 0, iv.length);
        System.arraycopy(epb, 0, allBytes, iv.length, epb.length);


        return Base64.getEncoder().encodeToString(allBytes);

    }
}
