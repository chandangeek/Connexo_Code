package com.energyict.protocolimplv2.umi.ei4;

import com.energyict.protocolimplv2.umi.security.scheme2.UmiCVCCertificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Logger;

public class Keys {
    public static final String KEY_PAIR_GENERATOR_ALGORITHM = "ECDSA";
    public static final String SECURITY_PROVIDER            = "BC";

    /** we have to hardcode those certificates & keys as EI4 devices do not implement the whole UMI S2 security */

    /** local device UMI certificate */
    private static final String ownCertBase64 = "fyGB639OgZ1fKQFAQggAI37//////1oBAH9JQ4ZBBFEtS3lWs5oC5guBjJyNtOby08" +
            "UrOYBTVN2vaVmGml50GpT11WD9RXk1Kvh8qTDhiVBUWpQLzVajZHU5V2PRFqJfIAgAI34AAAAABd8lDzIwMjEwMzIzMTAzOTU2Wt8k" +
            "DzIwNDEwMzIzMTAzOTU2Wl9MFAEAAAAAAAAAAAB//////////wYBXzdHMEUCIQCmiBWPWdNUKpaqK+tTa2QBDI0b9kXLEkfErcoX6E" +
            "TQyAIgTa9rBP7qs6wU5TB5f1OstIwaF33o6fMMfSGDPNPnpUM=";

    /** local device private key */
    private static final String ownPrivateKeyBase64 = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQguKw4cCZKr1cU3" +
            "u69lWbEEdHgX+TuP19qGoBibFkU5mqgCgYIKoZIzj0DAQehRANCAAQGzVwJSdF6rp+IDMFjzUJNPUspxqTjoEJmm3gAknD9/UYjDfpT" +
            "+VCzOELRiHavDdPEFxirXrQ0LZmvVsxlB2vn";

    /** remote communication module private key */
    private static final String remoteCMPrivateKeyBase64 = "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgFNY0NMEO" +
            "8E0el7+3RgHddb/gq1BVb1TApobg1ZAju6SgCgYIKoZIzj0DAQehRANCAAQGzVwJSdF6rp+IDMFjzUJNPUspxqTjoEJmm3gAknD9/UY" +
            "jDfpT+VCzOELRiHavDdPEFxirXrQ0LZmvVsxlB2vn";

    /** remote communication module UMI certificate */
    private static final String remoteCMCertBase64 = "fyGB7H9OgZ1fKQFAQggAI37//////1oBAH9JQ4ZBBKxG3uv5txdY2fdRGvPz" +
            "V6CMwtuDyT+Zx0E3TV6Cyck3x0V+EMXaeJ3zbEc4uc/iasnfCGLgYQNJlmDyhd92qNhfIAgCAAAAAAAAAd8lDzIwMjEwNTI4MTA0OTQ3W" +
            "t8kDzIwNDEwNTI4MTA0OTQ3Wl9MFAEAAAAAAAAAAAB//////////wYBXzdIMEYCIQCaHmUSno7DXbFcIlkYTkTNqazOy7SDaMtOWuE9Doi" +
            "0oAIhAKbIUVjFlxBnNWlFYpHJRSFA8Eln0jdSqytf6Mq0aYM8";

    /** remote HOST UMI certificate  0xEA, 0xD9 ...*/
    /*private static final String remoteCMCertBase64 = "fyGB639OgZ1fKQFAQggAI37//////1oBAH9JQ4ZBBOrZH1wRvEzmnrGQvT+FGA" +
            "4S5dt5UiTY/shwTIgV+ctFlUqLoRcPQ6QvEPLUroZuDPE+jFCQocPEzGzRlf/Wr5lfIAgCAAAAAAAAAd8lDzIwMjEwNTI4MTY1OTMzWt8" +
            "kDzIwNDEwNTI4MTY1OTMzWl9MFAEAAAAAAAAAAAB//////////wYBXzdHMEUCIEIbd+XDx+iv/WCpdbd4vzBKPeUjCRm0WA/j+YGGgQ/3" +
            "AiEAv7EC9CZkNSX08+GfsjvfE3hoZzKXJ+QAHN5vltUOwUk=";*/

    public static UmiCVCCertificate getOwnCert() {
        return getCertificate(ownCertBase64);
    }

    public static PrivateKey getOwnPrivateKey() {
        return getKey(ownPrivateKeyBase64);
    }

    public static UmiCVCCertificate getRemoteCMCert() {
        return getCertificate(remoteCMCertBase64);
    }

    public static PrivateKey getRemoteCMPrivateKey() {
        return getKey(remoteCMPrivateKeyBase64);
    }

    private static UmiCVCCertificate getCertificate(String certificateBase64) {
        UmiCVCCertificate cert = null;
        try {
            cert = new UmiCVCCertificate(Base64.getDecoder().decode(certificateBase64));
        } catch (Exception e) {
            Logger.getAnonymousLogger(e.getMessage());
        }
        return cert;
    }

    private static PrivateKey getKey(String keyBase64) {
        PrivateKey key = null;
        try {
            Security.addProvider(new BouncyCastleProvider());
            KeyFactory keyFactory = KeyFactory.getInstance(KEY_PAIR_GENERATOR_ALGORITHM, SECURITY_PROVIDER);
            key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyBase64)));
        } catch (Exception e) {
            Logger.getAnonymousLogger(e.getMessage());
        }
        return key;
    }
}
