package com.energyict.encryption.asymetric.keyagreement;

import com.energyict.encryption.AlgorithmID;
import com.energyict.encryption.asymetric.ECCCurve;
import com.energyict.encryption.asymetric.util.KeyUtils;
import com.energyict.encryption.kdf.KDF;
import com.energyict.encryption.kdf.NIST_SP_800_56_KDF;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/01/2016 - 13:28
 */
public class KeyAgreementImplTest {

    /**
     * The ephemeral public key of the client.
     */
    private static final byte[] EPHEMERAL_PUBLIC_KEY_CLIENT = ProtocolTools.getBytesFromHexString("2914D60E10AB705F62ED6CC349D7CB99B9AB3F3978E59" +
            "278C7AF595B3AF987941372DAB6D5AF1FA867E134167E" +
            "6F23DE664A6693E05F43414611058D1B48F894", "");

    /**
     * The ephemeral public key of the server.
     */
    private static final byte[] EPHEMERAL_PUBLIC_KEY_SERVER = ProtocolTools.getBytesFromHexString("95F41066009B185B074F5FFFF736B71C325FCADB2BC0C" +
            "F1A4F4B17BBE7AB81D62946506BC8169C7B539B39A5D8" +
            "463787F449C9BD2583FA67A1075B0DBFC638BA", "");

    /**
     * Another ephemeral public key of the server.
     */
    private static final byte[] EPHEMERAL_PUBLIC_KEY_SERVER2 = ProtocolTools.getBytesFromHexString("6439724714B47CD9CB988897D8424AB946DCD083D37A954637616011B9C2378773295F0F850D8DAFD1BBE9FE666E53E4F097CD10B38B69622152724A90987444", "");

    /**
     * The ephemeral private key of the server.
     */
    private static final byte[] EPHEMERAL_PRIVATE_KEY_SERVER = ProtocolTools.getBytesFromHexString("34A8C23A34DBB519D09B245754C85A6CFE05D14A063EF" +
            "A5AA41545AA8241EFAE", "");

    /**
     * The private key agreement key of the client
     */
    private static final byte[] PRIVATE_KEY_AGREEMENT_KEY_CLIENT = ProtocolTools.getBytesFromHexString("A51C16FF5C498FCC89323D4A9267CD71BF81FD6F6A891CD240DA7F3D6F283E65", "");

    /**
     * The public key agreement key of the client
     */
    private static final byte[] PUBLIC_KEY_AGREEMENT_KEY_CLIENT = ProtocolTools.getBytesFromHexString("07C56DE2DCAF0FD793EF29F019C89B4A0CC1E001CE94F4FFBE10BC05E7E66F7671A13FBCF9E662B9826FFF6A6938546D524ED6D3405F020296BDE16B04F7A7C2", "");

    /**
     * The expected shared secret.
     */
    private static final byte[] EXPECTED_SHARED_SECRET = ProtocolTools.getBytesFromHexString("C1CF8FE7891AEF3617D7190795E61FE6C24EFC3CCA2E0" +
            "8469BAD1A225CE6EA08", "");

    /**
     * One-pass ephemeral public key, for the client.
     */
    private static final byte[] ONEPASS_EPH_PUBLIC_KEY_CLIENT = ProtocolTools.getBytesFromHexString("C323C2BD45711DE4688637D919F92E9DB8FB2DFC213A88D21C9DC8DCBA917D8170511DE1BADB360D50058F794B0960AE11FA28D392CFF907A62D13E3357B1DC0", "");

    private static final byte[] ONEPASS_EPH_PRIVATE_KEY_CLIENT = ProtocolTools.getBytesFromHexString("47DAB03842E5B6E74828EF4F449B378D7DD1A5DAE1FFCA5AE0B0BE0AD18EC57E", "");
    private static final byte[] ONEPASS_STATIC_PRIVATE_KEY_SERVER = ProtocolTools.getBytesFromHexString("AAD3FD0732E991CF52A74C66C1F2827DDC53522A2E0A169D7C4FFCC0FB5D6A4D", "");

    private static final byte[] ONEPASS_STATIC_PUBLIC_KEY_SERVER = ProtocolTools.getBytesFromHexString("A653565B0E06070BAE9FBE140A5D2156812AEE2DD5250" +
            "53E3EFC850BF13BFDFFCB240BC7B77BFF5883344E7275" +
            "908D2287BEFA3725017295A096989D2338290B", "");

    private static final byte[] ONEPASS_EXPECTED_SHARED_SECRET_SERVER = ProtocolTools.getBytesFromHexString("0D4385BA0DD756CBCAB9887EB538396EE8F090A14C1079B4359F115B977F4615", "");
    private static final byte[] EXPECTED_GENERAL_CIPHERING_KEY_SERVER = ProtocolTools.getBytesFromHexString("8CD08FF02EAC71712DE8449DC8331833", "");

    private static final byte[] ONEPASS_EXPECTED_SHARED_SECRET_CLIENT = ProtocolTools.getBytesFromHexString("2B4302DC49790E2E78D990CFB52ED6E2F273DECE441A2D95E4301B93812A9FAC", "");
    private static final byte[] EXPECTED_GENERAL_CIPHERING_KEY_CLIENT = ProtocolTools.getBytesFromHexString("E357F06755CBF5C2C31457FE3CD1D5B8", "");

    /**
     * System title of the client ("Party U").
     */
    private static final byte[] SYSTEM_TITLE_CLIENT = ProtocolTools.getBytesFromHexString("4D4D4D0000BC614E", "");

    /**
     * System title of the server ("Party V").
     */
    private static final byte[] SYSTEM_TITLE_SERVER = ProtocolTools.getBytesFromHexString("4D4D4D0000000001", "");

    /**
     * The expected global unicast encryption key that is to be derived using the key agreement algorithm
     */
    private static final byte[] EXPECTED_GUEK = ProtocolTools.getBytesFromHexString("E025CA6F9EE8D2B40F993739D44CFBC0", "");

    /**
     * Tests Ephemeral Unified Model C(2e, 0s, ECC CDH) scheme
     * This scheme is used for rekeying, where both sides derive a new, secret key
     */
    @Test
    public final void testKeyAgreement() throws Exception {
        PublicKey publicKey = KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, EPHEMERAL_PUBLIC_KEY_SERVER);
        PrivateKey privateKey = KeyUtils.toECPrivateKey(ECCCurve.P256_SHA256, EPHEMERAL_PRIVATE_KEY_SERVER);
        final KeyAgreement agreement = new KeyAgreementImpl(ECCCurve.P256_SHA256, new KeyPair(publicKey, privateKey));

        final byte[] secret = agreement.generateSecret(KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, EPHEMERAL_PUBLIC_KEY_CLIENT));

        assertThat(secret).isEqualTo(EXPECTED_SHARED_SECRET);

        final KDF kdf = NIST_SP_800_56_KDF.getInstance();
        final byte[] key = kdf.derive(KDF.HashFunction.SHA256, secret, AlgorithmID.AES_GCM_128, SYSTEM_TITLE_CLIENT, SYSTEM_TITLE_SERVER);

        assertArrayEquals(key, EXPECTED_GUEK);
    }

    /**
     * Derive a secret key on both sides (client & server), they should result in the same secret key.
     * Using predefined key pairs on both sides
     */
    @Test
    public final void testKeyAgreementFixedOnBothSides() throws Exception {
        PublicKey publicKey = KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, ONEPASS_EPH_PUBLIC_KEY_CLIENT);
        PrivateKey privateKey = KeyUtils.toECPrivateKey(ECCCurve.P256_SHA256, ONEPASS_EPH_PRIVATE_KEY_CLIENT);
        KeyAgreement agreement = new KeyAgreementImpl(ECCCurve.P256_SHA256, new KeyPair(publicKey, privateKey));

        //Generate the secret, using our private and key and the public key of the other party
        byte[] clientSecret = agreement.generateSecret(KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, EPHEMERAL_PUBLIC_KEY_SERVER));

        publicKey = KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, EPHEMERAL_PUBLIC_KEY_SERVER);
        privateKey = KeyUtils.toECPrivateKey(ECCCurve.P256_SHA256, EPHEMERAL_PRIVATE_KEY_SERVER);
        agreement = new KeyAgreementImpl(ECCCurve.P256_SHA256, new KeyPair(publicKey, privateKey));

        //Generate the secret, using our private and key and the public key of the other party
        final byte[] serverSecret = agreement.generateSecret(KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, ONEPASS_EPH_PUBLIC_KEY_CLIENT));

        assertArrayEquals(clientSecret, serverSecret);
    }

    /**
     * Derive a secret key on both sides (client & server), they should result in the same secret key.
     * Using key pairs on both sides that were generated at runtime (so ephemeral keys)
     */
    @Test
    public final void testKeyAgreementEphemeralOnBothSides() throws Exception {
        KeyAgreement agreementClientSide = new KeyAgreementImpl(ECCCurve.P256_SHA256);
        KeyAgreement agreementServerSide = new KeyAgreementImpl(ECCCurve.P256_SHA256);

        //Generate the secret, using our private and key and the public key of the other party
        byte[] clientSecret = agreementClientSide.generateSecret(agreementServerSide.getEphemeralPublicKey());
        byte[] serverSecret = agreementServerSide.generateSecret(agreementClientSide.getEphemeralPublicKey());

        assertArrayEquals(clientSecret, serverSecret);

        final KDF kdf = NIST_SP_800_56_KDF.getInstance();
        final byte[] key = kdf.derive(KDF.HashFunction.SHA256, clientSecret, AlgorithmID.AES_GCM_128, SYSTEM_TITLE_CLIENT, SYSTEM_TITLE_SERVER);
        assertEquals(key.length, 16);
    }

    /**
     * Derive a secret key on both sides (client & server), they should result in the same secret key.
     * Using key pairs on both sides that were generated at runtime
     */
    @Test
    public final void testKeyAgreementEphemeralOnBothSides_P384() throws Exception {
        KeyAgreement agreementClientSide = new KeyAgreementImpl(ECCCurve.P384_SHA384);
        KeyAgreement agreementServerSide = new KeyAgreementImpl(ECCCurve.P384_SHA384);

        //Generate the secret, using our private and key and the public key of the other party
        byte[] clientSecret = agreementClientSide.generateSecret(agreementServerSide.getEphemeralPublicKey());
        byte[] serverSecret = agreementServerSide.generateSecret(agreementClientSide.getEphemeralPublicKey());

        assertArrayEquals(clientSecret, serverSecret);

        final KDF kdf = NIST_SP_800_56_KDF.getInstance();
        final byte[] key = kdf.derive(KDF.HashFunction.SHA384, clientSecret, AlgorithmID.AES_GCM_256, SYSTEM_TITLE_CLIENT, SYSTEM_TITLE_SERVER);
        assertEquals(key.length, 32);
    }

    /**
     * Tests One-Pass Diffie-Hellman C(1e, 1s, ECC CDH) scheme, server side calculations (when receiving a frame from the client)
     * This is used to generate a session key that is then used to encrypt the APDUs.
     *
     * @throws Exception In case of an error.
     */
    @Test
    public final void testOnePassToServer() throws Exception {
        PublicKey publicKey = KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, ONEPASS_STATIC_PUBLIC_KEY_SERVER);
        PrivateKey privateKey = KeyUtils.toECPrivateKey(ECCCurve.P256_SHA256, ONEPASS_STATIC_PRIVATE_KEY_SERVER);
        final KeyAgreement agreement = new KeyAgreementImpl(ECCCurve.P256_SHA256, new KeyPair(publicKey, privateKey));

        final byte[] secret = agreement.generateSecret(KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, ONEPASS_EPH_PUBLIC_KEY_CLIENT));
        assertThat(secret).isEqualTo(ONEPASS_EXPECTED_SHARED_SECRET_SERVER);

        final KDF kdf = NIST_SP_800_56_KDF.getInstance();
        final byte[] key = kdf.derive(KDF.HashFunction.SHA256, secret, AlgorithmID.AES_GCM_128, SYSTEM_TITLE_CLIENT, SYSTEM_TITLE_SERVER);
        assertThat(key).isEqualTo(EXPECTED_GENERAL_CIPHERING_KEY_SERVER);
    }

    /**
     * Tests One-Pass Diffie-Hellman C(1e, 1s, ECC CDH) scheme, client side calculations. (when receiving a frame from the server)
     * This is used to generate a session key that is then used to encrypt the APDUs.
     *
     * @throws Exception In case of an error.
     */
    @Test
    public final void testOnePassToClient() throws Exception {
        PublicKey publicKey = KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, PUBLIC_KEY_AGREEMENT_KEY_CLIENT);
        PrivateKey privateKey = KeyUtils.toECPrivateKey(ECCCurve.P256_SHA256, PRIVATE_KEY_AGREEMENT_KEY_CLIENT);
        KeyAgreement agreement = new KeyAgreementImpl(ECCCurve.P256_SHA256, new KeyPair(publicKey, privateKey));

        byte[] secret = agreement.generateSecret(KeyUtils.toECPublicKey(ECCCurve.P256_SHA256, EPHEMERAL_PUBLIC_KEY_SERVER2));
        assertThat(secret).isEqualTo(ONEPASS_EXPECTED_SHARED_SECRET_CLIENT);

        final KDF kdf = NIST_SP_800_56_KDF.getInstance();
        final byte[] key = kdf.derive(KDF.HashFunction.SHA256, secret, AlgorithmID.AES_GCM_128, SYSTEM_TITLE_SERVER, SYSTEM_TITLE_CLIENT);
        assertThat(key).isEqualTo(EXPECTED_GENERAL_CIPHERING_KEY_CLIENT);
    }

    /**
     * Test rekeying using the Ephemeral Unified Model C(2e, 0s, ECC CDH) scheme, suite 1
     */
    @Test
    public void testRekeyingSuite1() throws GeneralSecurityException {
        KeyPair ephemeralClientKeyPair = KeyUtils.generateECCKeyPair(ECCCurve.P256_SHA256);
        KeyPair ephemeralServerKeyPair = KeyUtils.generateECCKeyPair(ECCCurve.P256_SHA256);

        byte[] clientSecret = new KeyAgreementImpl(ECCCurve.P256_SHA256, ephemeralClientKeyPair).generateSecret(ephemeralServerKeyPair.getPublic());
        byte[] serverSecret = new KeyAgreementImpl(ECCCurve.P256_SHA256, ephemeralServerKeyPair).generateSecret(ephemeralClientKeyPair.getPublic());

        assertArrayEquals(clientSecret, serverSecret);
    }

    /**
     * Test rekeying using the Ephemeral Unified Model C(2e, 0s, ECC CDH) scheme, suite 2
     */
    @Test
    public void testRekeyingSuite2() throws GeneralSecurityException {
        KeyPair ephemeralClientKeyPair = KeyUtils.generateECCKeyPair(ECCCurve.P384_SHA384);
        KeyPair ephemeralServerKeyPair = KeyUtils.generateECCKeyPair(ECCCurve.P384_SHA384);

        byte[] clientSecret = new KeyAgreementImpl(ECCCurve.P384_SHA384, ephemeralClientKeyPair).generateSecret(ephemeralServerKeyPair.getPublic());
        byte[] serverSecret = new KeyAgreementImpl(ECCCurve.P384_SHA384, ephemeralServerKeyPair).generateSecret(ephemeralClientKeyPair.getPublic());

        assertArrayEquals(clientSecret, serverSecret);
    }
}