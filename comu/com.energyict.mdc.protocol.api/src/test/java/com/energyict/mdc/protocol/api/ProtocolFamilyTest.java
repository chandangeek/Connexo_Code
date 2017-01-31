/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Tests the {@link ProtocolFamily} component.
 */
public class ProtocolFamilyTest {

    @Test
    public void testAllUniqueCodes () {
        Set<Integer> alreadyUsedCodes = new HashSet<>();
        for (ProtocolFamily rule : ProtocolFamily.values()) {
            if (!alreadyUsedCodes.add(rule.getCode())) {
                fail("FamilyRule enum value " + rule + " is NOT using a unique code: " + rule.getCode());
            }
        }
    }

    @Test
    public void testFromNonExistingCode () {
        assertThat(ProtocolFamily.fromCode(0)).isNull();
    }

    @Test
    public void testFromCode () {
        for (ProtocolFamily rule : ProtocolFamily.values()) {
            if (!ProtocolFamily.fromCode(rule.getCode()).equals(rule)) {
                fail("ProtocolFamily#fromCode is not returning the expected value for " + rule);
            }
        }
    }

}