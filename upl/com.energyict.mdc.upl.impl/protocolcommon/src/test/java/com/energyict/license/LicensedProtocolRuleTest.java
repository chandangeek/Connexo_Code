package com.energyict.license;

import org.junit.*;

import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

/**
 * Tests the {@link LicensedProtocolRule} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-18 (12:41)
 */
public class LicensedProtocolRuleTest {

    @Test
    public void testAllUniqueCodes () {
        Set<Integer> alreadyUsedCodes = new HashSet<>();
        for (LicensedProtocolRule rule : LicensedProtocolRule.values()) {
            if (!alreadyUsedCodes.add(rule.getCode())) {
                fail("LicensedProtocolRule enum value " + rule + " is NOT using a unique code: " + rule.getCode());
            }
        }
    }

    @Test
    public void testFromNonExistingCode () {
        assertThat(LicensedProtocolRule.fromCode(0)).isNull();
    }

    @Test
    public void testFromCode () {
        for (LicensedProtocolRule rule : LicensedProtocolRule.values()) {
            if (!LicensedProtocolRule.fromCode(rule.getCode()).equals(rule)) {
                fail("LicensedProtocolRule#fromCode is not returning the expected value for " + rule);
            }
        }
    }

    @Test
    public void testFromNonExistingClassName () {
        assertThat(LicensedProtocolRule.fromClassName(LicensedProtocolRuleTest.class.getName())).isNull();
    }

    @Test
    public void testFromClassName () {
        for (LicensedProtocolRule rule : LicensedProtocolRule.values()) {
            if (!LicensedProtocolRule.fromClassName(rule.getClassName()).equals(rule)) {
                fail("LicensedProtocolRule#fromClassName is not returning the expected value for " + rule);
            }
        }
    }

    @Test
    public void testFamiliesNotNull () {
        for (LicensedProtocolRule rule : LicensedProtocolRule.values()) {
            if (rule.getFamilies() == null) {
                fail("LicensedProtocolRule#getFamilies should not return null for " + rule);
            }
        }
    }

}