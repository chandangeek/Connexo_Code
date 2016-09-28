package com.energyict.dlms.aso;

import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/02/2016 - 13:53
 */
public class SecurityPolicyTest {

    @Test
    public void testAuthAndEncrAndSignedPolicy() {
        SecurityPolicy securityPolicy = new SecurityPolicy(1, 0xFC);
        assertTrue(securityPolicy.isRequestAuthenticatedAndEncrypted());
        assertFalse(securityPolicy.isRequestAuthenticatedOnly());
        assertFalse(securityPolicy.isRequestEncryptedOnly());
        assertFalse(securityPolicy.isRequestPlain());
        assertTrue(securityPolicy.isResponseAuthenticatedAndEncrypted());
        assertFalse(securityPolicy.isResponseAuthenticatedOnly());
        assertFalse(securityPolicy.isResponseEncryptedOnly());
        assertFalse(securityPolicy.isResponsePlain());
    }

    @Test
    public void testAuthAndEncrPolicySuite0() {
        SecurityPolicy securityPolicy = new SecurityPolicy(0, 3);
        assertTrue(securityPolicy.isRequestAuthenticatedAndEncrypted());
        assertFalse(securityPolicy.isRequestAuthenticatedOnly());
        assertFalse(securityPolicy.isRequestEncryptedOnly());
        assertFalse(securityPolicy.isRequestPlain());
        assertTrue(securityPolicy.isResponseAuthenticatedAndEncrypted());
        assertFalse(securityPolicy.isResponseAuthenticatedOnly());
        assertFalse(securityPolicy.isResponseEncryptedOnly());
        assertFalse(securityPolicy.isResponsePlain());
    }

    @Test
    public void testSignedOnlyPolicy() {
        SecurityPolicy securityPolicy = new SecurityPolicy(1, 0x90);
        assertFalse(securityPolicy.isRequestAuthenticatedAndEncrypted());
        assertFalse(securityPolicy.isRequestAuthenticatedOnly());
        assertFalse(securityPolicy.isRequestEncryptedOnly());
        assertTrue(securityPolicy.isRequestPlain());
        assertFalse(securityPolicy.isResponseAuthenticatedAndEncrypted());
        assertFalse(securityPolicy.isResponseAuthenticatedOnly());
        assertFalse(securityPolicy.isResponseEncryptedOnly());
        assertTrue(securityPolicy.isResponsePlain());
    }

    @Test
    public void testRequestEncryptedAndResponseAuthenticated() {
        SecurityPolicy securityPolicy = new SecurityPolicy(1, 0x28);
        assertFalse(securityPolicy.isRequestAuthenticatedAndEncrypted());
        assertFalse(securityPolicy.isRequestAuthenticatedOnly());
        assertTrue(securityPolicy.isRequestEncryptedOnly());
        assertFalse(securityPolicy.isRequestPlain());
        assertFalse(securityPolicy.isResponseAuthenticatedAndEncrypted());
        assertTrue(securityPolicy.isResponseAuthenticatedOnly());
        assertFalse(securityPolicy.isResponseEncryptedOnly());
        assertFalse(securityPolicy.isResponsePlain());
    }
}