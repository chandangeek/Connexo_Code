package com.energyict.protocolimplv2.umi.session;

import com.energyict.protocolimplv2.umi.ei4.Keys;
import com.energyict.protocolimplv2.umi.security.scheme2.UmiCVCCertificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class UmiSessionS2UtilsTest {
    public static String localPrivateKeyBase64 = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQguKw4cCZKr1cU3" +
            "u69lWbEEdHgX+TuP19qGoBibFkU5mqgCgYIKoZIzj0DAQehRANCAAQGzVwJSdF6rp+IDMFjzUJNPUspxqTjoEJmm3gAknD9/UYjDfpT" +
            "+VCzOELRiHavDdPEFxirXrQ0LZmvVsxlB2vn";

    public static String localCertBase64 = "fyGB639OgZ1fKQFAQggAI37//////1oBAH9JQ4ZBBFEtS3lWs5oC5guBjJyNtOby08" +
            "UrOYBTVN2vaVmGml50GpT11WD9RXk1Kvh8qTDhiVBUWpQLzVajZHU5V2PRFqJfIAgAI34AAAAABd8lDzIwMjEwMzIzMTAzOTU2Wt8k" +
            "DzIwNDEwMzIzMTAzOTU2Wl9MFAEAAAAAAAAAAAB//////////wYBXzdHMEUCIQCmiBWPWdNUKpaqK+tTa2QBDI0b9kXLEkfErcoX6E" +
            "TQyAIgTa9rBP7qs6wU5TB5f1OstIwaF33o6fMMfSGDPNPnpUM=";

    public static String remotePrivateKeyBase64 = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgFNY0NMEO" +
            "8E0el7+3RgHddb/gq1BVb1TApobg1ZAju6SgCgYIKoZIzj0DAQehRANCAAQGzVwJSdF6rp+IDMFjzUJNPUspxqTjoEJmm3gAknD9/UY" +
            "jDfpT+VCzOELRiHavDdPEFxirXrQ0LZmvVsxlB2vn";

    public static String remoteCertBase64 =
            "fyGB639OgZ1fKQFAQggAI37//////1oBAH9JQ4ZBBOrZH1wRvEzmnrGQvT+FGA" +
                    "4S5dt5UiTY/shwTIgV+ctFlUqLoRcPQ6QvEPLUroZuDPE+jFCQocPEzGzRlf/Wr5lfIAgCAAAAAAAAAd8lDzIwMjEwNTI4MTY1OTMzWt8" +
                    "kDzIwNDEwNTI4MTY1OTMzWl9MFAEAAAAAAAAAAAB//////////wYBXzdHMEUCIEIbd+XDx+iv/WCpdbd4vzBKPeUjCRm0WA/j+YGGgQ/3" +
                    "AiEAv7EC9CZkNSX08+GfsjvfE3hoZzKXJ+QAHN5vltUOwUk=";

    public static String remoteCertRole6Base64 = "fyGB7H9OgZ1fKQFAQggAI37//////1oBAH9JQ4ZBBKxG3uv5txdY2fdRGvPz" +
            "V6CMwtuDyT+Zx0E3TV6Cyck3x0V+EMXaeJ3zbEc4uc/iasnfCGLgYQNJlmDyhd92qNhfIAgCAAAAAAAAAd8lDzIwMjEwNTI4MTA0OTQ3W" +
            "t8kDzIwNDEwNTI4MTA0OTQ3Wl9MFAEAAAAAAAAAAAB//////////wYBXzdIMEYCIQCaHmUSno7DXbFcIlkYTkTNqazOy7SDaMtOWuE9Doi" +
            "0oAIhAKbIUVjFlxBnNWlFYpHJRSFA8Eln0jdSqytf6Mq0aYM8";


    public static PrivateKey getLocalKey() throws Exception {
        byte[] localPrivateKey = Base64.getDecoder().decode(localPrivateKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance(Keys.KEY_PAIR_GENERATOR_ALGORITHM, Keys.SECURITY_PROVIDER);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(localPrivateKey));
    }

    public static UmiCVCCertificate getLocalCert() throws Exception {
        byte[] localCert = Base64.getDecoder().decode(localCertBase64);
        return new UmiCVCCertificate(localCert);
    }

    public static UmiCVCCertificate getRemoteCert() throws Exception {
        byte[] remoteCert = Base64.getDecoder().decode(remoteCertBase64);
        return new UmiCVCCertificate(remoteCert);
    }

    public static UmiCVCCertificate getRemoteCertRole6() throws Exception {
        byte[] remoteCert = Base64.getDecoder().decode(remoteCertRole6Base64);
        return new UmiCVCCertificate(remoteCert);
    }

    public static PrivateKey getRemoteKey() throws Exception {
        byte[] remotePrivateKey = Base64.getDecoder().decode(remotePrivateKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance(Keys.KEY_PAIR_GENERATOR_ALGORITHM, Keys.SECURITY_PROVIDER);
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(remotePrivateKey));
    }

    @Test
    public void testCreateSessionKey() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        PrivateKey localKey = getLocalKey();
        PrivateKey remoteKey = getRemoteKey();

        UmiCVCCertificate localUmiCert = getLocalCert();
        UmiCVCCertificate remoteUmiCert = getRemoteCert();

        byte[] saltA = UmiSessionS2Utils.generateSalt();
        byte[] saltB = UmiSessionS2Utils.generateSalt();

        byte[] localSessionKey = UmiSessionS2Utils.createSessionKey(localKey, remoteUmiCert, saltA, saltB);
        byte[] remoteSessionKey = UmiSessionS2Utils.createSessionKey(remoteKey, localUmiCert, saltA, saltB);

        assertArrayEquals(localSessionKey, remoteSessionKey);
    }

    @Test
    public void testGenerateSalt() throws Exception {
        byte[] salt = UmiSessionS2Utils.generateSalt();
        assertEquals(UmiSessionS2Utils.SALT_LENGTH, salt.length);
    }


} 
