package com.energyict.protocolimplv2.eict.eiweb;

import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link StringBasedMD5Seed} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-19 (15:19)
 */
public class StringBasedMD5SeedTest {

    private static final String SEED_VALUE = "StringBasedMD5Seed";

    @Test
    public void testGetBytes () {
        StringBasedMD5Seed md5Seed = new StringBasedMD5Seed(SEED_VALUE);
        assertThat(md5Seed.getBytes()).isNotNull();
    }

}