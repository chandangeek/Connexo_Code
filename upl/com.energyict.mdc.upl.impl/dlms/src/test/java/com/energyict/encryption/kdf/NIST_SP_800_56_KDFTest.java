package com.energyict.encryption.kdf;

import com.energyict.encryption.AlgorithmID;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the NIST SP 800-56 KDF.
 *
 * @author alex
 */
public final class NIST_SP_800_56_KDFTest {

    /**
     * The Z input.
     */
    private static final byte[] Z_CLIENT = ProtocolTools.getBytesFromHexString("C1CF8FE7891AEF3617D7190795E61FE6C24EFC3CCA2E08469BAD1A225CE6EA08", "");

    private static final byte[] Z_SERVER = ProtocolTools.getBytesFromHexString("2B4302DC49790E2E78D990CFB52ED6E2F273DECE441A2D95E4301B93812A9FAC", "");

    /**
     * System title of the client ("Party U").
     */
    private static final byte[] SYSTEM_TITLE_CLIENT = ProtocolTools.getBytesFromHexString("4D4D4D0000BC614E", "");

    /**
     * System title of the server ("Party V").
     */
    private static final byte[] SYSTEM_TITLE_SERVER = ProtocolTools.getBytesFromHexString("4D4D4D0000000001", "");

    /**
     * Expected key when using a compliant NIST KDF.
     */
    private static final byte[] EXPECTED_GUEK_CLIENT = ProtocolTools.getBytesFromHexString("E025CA6F9EE8D2B40F993739D44CFBC0", "");

    private static final byte[] EXPECTED_GUEK_SERVER = ProtocolTools.getBytesFromHexString("E357F06755CBF5C2C31457FE3CD1D5B8", "");

    /**
     * Tests the KDF.
     */
    @Test
    public final void testKDFClient() throws Exception {
        final KDF kdf = NIST_SP_800_56_KDF.getInstance();

        final byte[] key = kdf.derive(KDF.HashFunction.SHA256, Z_CLIENT, AlgorithmID.AES_GCM_128, SYSTEM_TITLE_CLIENT, SYSTEM_TITLE_SERVER);

        assertThat(key).isEqualTo(EXPECTED_GUEK_CLIENT);
    }

    /**
     * Tests the KDF.
     */
    @Test
    public final void testKDFServer() throws Exception {
        final KDF kdf = NIST_SP_800_56_KDF.getInstance();

        final byte[] key = kdf.derive(KDF.HashFunction.SHA256, Z_SERVER, AlgorithmID.AES_GCM_128, SYSTEM_TITLE_SERVER, SYSTEM_TITLE_CLIENT);

        assertThat(key).isEqualTo(EXPECTED_GUEK_SERVER);
    }
}
