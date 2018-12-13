package com.energyict.license;

import org.junit.*;

import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

/**
 * Tests the {@link FamilyRule} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-07-18 (14:37)
 */
public class FamilyRuleTest {

    @Test
    public void testAllUniqueCodes () {
        Set<Integer> alreadyUsedCodes = new HashSet<>();
        for (FamilyRule rule : FamilyRule.values()) {
            if (!alreadyUsedCodes.add(rule.getCode())) {
                fail("FamilyRule enum value " + rule + " is NOT using a unique code: " + rule.getCode());
            }
        }
    }

    @Test
    public void testFromNonExistingCode () {
        assertThat(FamilyRule.fromCode(0)).isNull();
    }

    @Test
    public void testFromCode () {
        for (FamilyRule rule : FamilyRule.values()) {
            if (!FamilyRule.fromCode(rule.getCode()).equals(rule)) {
                fail("FamilyRule#fromCode is not returning the expected value for " + rule);
            }
        }
    }

}