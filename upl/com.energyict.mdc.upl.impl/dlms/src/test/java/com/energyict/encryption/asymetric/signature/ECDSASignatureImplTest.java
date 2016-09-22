package com.energyict.encryption.asymetric.signature;

import com.energyict.encryption.asymetric.ECCCurve;
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the ECDSA signatures for suite 1 and 2. We use the test vectors of the Green Book here.
 *
 * @author alex
 */
public final class ECDSASignatureImplTest {

    /**
     * Taken from the Green Book, 8, Table C.1., p451.
     */
    private static final byte[] TEST_PUB_KEY_P256 = ProtocolTools.getBytesFromHexString("BAAFFDE06A8CB1C9DAE8D94023C601DBBB249254BA22E" +
            "DD827E820BCA2BCC64362FBB83D86A82B87BB8B7161D2" +
            "AAB5521911A946B97A284A90F7785CD9047D25", "");

    /**
     * The private key.
     */
    private static final byte[] TEST_PRIV_KEY_P256 = ProtocolTools.getBytesFromHexString("418073C239FA6125011DE4D6CD2E645780289F761BB21" +
            "BFB0835CB5585E8B373", "");

    /**
     * The data used as input to the sign operation (key_id || EPUB_KS).
     */
    private static final byte[] TEST_SIGN_DATA_P256 = ProtocolTools.getBytesFromHexString("002914D60E10AB705F62ED6CC349D7CB99B9AB3F3978E59" +
            "278C7AF595B3AF987941372DAB6D5AF1FA867E134167E" +
            "6F23DE664A6693E05F43414611058D1B48F894", "");

    /**
     * The signature : Epub-K-Sig-C.
     */
    private static final byte[] TEST_SIGNATURE_P256 = ProtocolTools.getBytesFromHexString("06F0607702AA0E2435A183E2F6B1ECD19629712E389A2" +
            "13610C03F77B2590860EA840AF5C3FA1F2BCDF055D474" +
            "4E9A01CE9A0E55026BCAA4EEBEB764CED64BB3", "");

    /**
     * The signature : Epub-K-Sig-C.
     */
    private static final byte[] TEST_SIGNATURE_P256_INVALID = ProtocolTools.getBytesFromHexString("06F0607702AA0E2435A183E2F6B1ECD19629712E389A2" +
            "13610C03F77B2590860EA840AF5C3FA1F2BCDF055D474" +
            "4E9A01CE9A0E55026BCAA4EEBEB764CED64FB3", "");

    /**
     * Since there's no references in the green book for this one, this is from "Suite B Implementer’s Guide to FIPS 186-3 (ECDSA)".
     */
    private static final byte[] TEST_PRIV_KEY_P384 = ProtocolTools.getBytesFromHexString("c838b85253ef8dc7394fa5808a5183981c7deef5a69ba8f4" +
            "f2117ffea39cfcd90e95f6cbc854abacab701d50c1f3cf24", "");

    /**
     * Same here.
     */
    private static final byte[] TEST_PUB_KEY_P384 = ProtocolTools.getBytesFromHexString("1fbac8eebd0cbf35640b39efe0808dd774debff20a2a329e" +
            "91713baf7d7f3c3e81546d883730bee7e48678f857b02ca0" +
            "eb213103bd68ce343365a8a4c3d4555fa385f5330203bdd7" +
            "6ffad1f3affb95751c132007e1b240353cb0a4cf1693bdf9", "");

    /**
     * The data used for signing.
     */
    private static final byte[] TEST_SIGN_DATA_P384 = ProtocolTools.getBytesFromHexString("54686973206973206f6e6c7920612074657374206d657373" +
            "6167652e204974206973203438206279746573206c6f6e67", "");

    /**
     * The signature itself.
     */
    private static final byte[] TEST_SIGNATURE_P384 = ProtocolTools.getBytesFromHexString("a0c27ec893092dea1e1bd2ccfed3cf945c8134ed0c9f8131" +
            "1a0f4a05942db8dbed8dd59f267471d5462aa14fe72de856" +
            "20ab3f45b74f10b6e11f96a2c8eb694d206b9dda86d3c7e3" +
            "31c26b22c987b7537726577667adadf168ebbe803794a402", "");

    /**
     * The signature itself.
     */
    private static final byte[] TEST_SIGNATURE_P384_INVALID = ProtocolTools.getBytesFromHexString("a0c27ec893092dea1e1bd2ccfed3cf945c8134ed0c9f8131" +
            "1a0f4a05942db8dbed8dd59f267471d5462aa14fe72de856" +
            "20ab3f45b74f10b6e11f96a2c8eb694d206b9dda86d3c7e3" +
            "31c26b22c987b7537726577667adadf168ebbe803794a502", "");
    private static final byte[] GREENBOOK_SIGNATURE_CLIENT = ProtocolTools.getBytesFromHexString("B51BE089D0B682863B2217201E73A1A9031968A9B4121DCBC3281A69739AF87429F5B3AC5471E7B6A04A2C0F2F8A25FD772A317DF97FC5463FEAC248EB8AB8BE", "");

    private static final byte[] ONEPASS_EPH_PUBLIC_KEY_CLIENT = ProtocolTools.getBytesFromHexString("C323C2BD45711DE4688637D919F92E9DB8FB2DFC213A8" +
            "8D21C9DC8DCBA917D8170511DE1BADB360D50058F794B" +
            "0960AE11FA28D392CFF907A62D13E3357B1DC0", "");

    /**
     * Another ephemeral public key of the server.
     */
    private static final byte[] EPHEMERAL_PUBLIC_KEY_SERVER2 = ProtocolTools.getBytesFromHexString("6439724714B47CD9CB988897D8424AB946DCD083D37A954637616011B9C2378773295F0F850D8DAFD1BBE9FE666E53E4F097CD10B38B69622152724A90987444", "");
    private static final byte[] PRIVATE_SIGNING_KEY_SERVER = ProtocolTools.getBytesFromHexString("AE55414FFE079F9FC95649536BD1C2B5653D200813727E07D501A8B550C69207", "");
    private static final byte[] PUBLIC_SIGNING_KEY_SERVER = ProtocolTools.getBytesFromHexString("933ACF15B03A9248E029B2787FB52A0AECAF635F07C42A0019FB3197E38F8F549A125EA36781B0CA96BE89A0E1FE2CF9B7361ED48B3C5E24592B9C0F4EDD31D1", "");
    private static final byte[] GREENBOOK_SIGNATURE_SERVER = ProtocolTools.getBytesFromHexString("E1FF47974A1F6931A6502F58147463F0E8CC517D47F55B0AC56DD8AC5C9D0E481934F2D90F9893016BD82B6E3FFE21FF1588F3278B4E9D98EB4FB62ADD64B380", "");

    /**
     * Tests the verification.
     */
    @Test
    public final void testVerifyP256() throws Exception {
        final DigitalSignature sig = new ECDSASignatureImpl(ECCCurve.P256_SHA256);
        final PublicKey pubKey = KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, TEST_PUB_KEY_P256);

        assertThat(sig.verify(TEST_SIGN_DATA_P256, TEST_SIGNATURE_P256, pubKey)).isTrue();
        assertThat(sig.verify(TEST_SIGN_DATA_P256, TEST_SIGNATURE_P256_INVALID, pubKey)).isFalse();
    }

    /**
     * Tests the verification.
     */

    public final void testVerifyP384() throws Exception {
        final DigitalSignature sig = new ECDSASignatureImpl(ECCCurve.P384_SHA384);
        final PublicKey pubKey = KeyUtils.toECPublicKey(ECCCurve.P384_SHA384, TEST_PUB_KEY_P384);

        assertThat(sig.verify(TEST_SIGN_DATA_P384, TEST_SIGNATURE_P384, pubKey)).isTrue();
        assertThat(sig.verify(TEST_SIGN_DATA_P384, TEST_SIGNATURE_P384_INVALID, pubKey)).isFalse();
    }

    /**
     * Tests the sign and verify.
     */

    public final void testSignAndVerifyP384() throws Exception {
        final DigitalSignature sig = new ECDSASignatureImpl(ECCCurve.P384_SHA384);
        final PrivateKey privateKey = KeyUtils.toECPrivateKey(ECCCurve.P384_SHA384, TEST_PRIV_KEY_P384);
        final PublicKey pubKey = KeyUtils.toECPublicKey(ECCCurve.P384_SHA384, TEST_PUB_KEY_P384);

        final byte[] signature = sig.sign(TEST_SIGN_DATA_P384, privateKey);

        assertThat(sig.verify(TEST_SIGN_DATA_P384, signature, pubKey)).isTrue();
    }

    /**
     * Tests the sign and verify.
     */

    public final void testSignAndVerifyP256() throws Exception {
        final DigitalSignature sig = new ECDSASignatureImpl(ECCCurve.P256_SHA256);
        final PrivateKey privateKey = KeyUtils.toECPrivateKey(ECCCurve.P256_SHA256, TEST_PRIV_KEY_P256);
        final PublicKey pubKey = KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, TEST_PUB_KEY_P256);

        final byte[] signature = sig.sign(TEST_SIGN_DATA_P256, privateKey);

        assertThat(sig.verify(TEST_SIGN_DATA_P256, signature, pubKey)).isTrue();
    }


    /**
     * Tests the sign and verify.
     */
    @Test
    public final void testSignAndVerifyAPublicKey() throws Exception {
        ECDSASignatureImpl ecdsaSignature = new ECDSASignatureImpl(ECCCurve.P256_SHA256);
        byte[] manualSignature = ecdsaSignature.sign(ONEPASS_EPH_PUBLIC_KEY_CLIENT, KeyUtils.toECPrivateKey(ECCCurve.P256_SHA256, TEST_PRIV_KEY_P256));

        ecdsaSignature.verify(ONEPASS_EPH_PUBLIC_KEY_CLIENT, manualSignature, KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, TEST_PUB_KEY_P256));
        ecdsaSignature.verify(ONEPASS_EPH_PUBLIC_KEY_CLIENT, GREENBOOK_SIGNATURE_CLIENT, KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, TEST_PUB_KEY_P256));

        ecdsaSignature = new ECDSASignatureImpl(ECCCurve.P256_SHA256);
        manualSignature = ecdsaSignature.sign(EPHEMERAL_PUBLIC_KEY_SERVER2, KeyUtils.toECPrivateKey(ECCCurve.P256_SHA256, PRIVATE_SIGNING_KEY_SERVER));

        ecdsaSignature.verify(EPHEMERAL_PUBLIC_KEY_SERVER2, manualSignature, KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, PUBLIC_SIGNING_KEY_SERVER));
        ecdsaSignature.verify(EPHEMERAL_PUBLIC_KEY_SERVER2, GREENBOOK_SIGNATURE_SERVER, KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, PUBLIC_SIGNING_KEY_SERVER));
    }
}