/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

import com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MessageSeedsTest {

    @Test
    public void testAllSeedsHaveUniqueNumber() {
        Set<Integer> uniqueIds = new HashSet<>();
        for (com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds messageSeed : com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.values()) {
            assertThat(uniqueIds.add(messageSeed.getNumber())).as(messageSeed.name() + " does not have a unique number")
                    .isTrue();
        }
    }

    @Test
    public void testAllMessageSeedsHaveUniqueKeys() {
        Set<String> uniqueKeys = new HashSet<>();
        for (com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds messageSeed : com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.values()) {
            assertThat(uniqueKeys.add(messageSeed.getKey())).as(messageSeed.name() + " does not have a unique key")
                    .isTrue();
        }
    }

    @Test
    public void testAllSeedsHaveNonNullKey() {
        for (com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds messageSeed : com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.values()) {
            assertThat(messageSeed.getKey()).as(messageSeed.name() + " has null key").isNotNull();
        }
    }

    @Test
    public void testKeyDoesntStartOrEndWithANonPrintableChar() {
        for (com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds messageSeed : com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.values()) {
            String key = messageSeed.getKey();
            assertThat(key.trim().length()).as(messageSeed.name() + " has empty key").isNotZero()
                    .as(messageSeed.name() + " key should not start or end with a non-printable character")
                    .isEqualTo(key.length());
        }
    }

    @Test
    public void testAllMessageSeedKeysAreWithinLengthLimit() {
        for (com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds messageSeed : com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.values()) {
            assertThat(messageSeed.getKey().length()).as(messageSeed.name() + " key is longer than max of 256")
                    .isLessThanOrEqualTo(256);
        }
    }

    @Test
    public void testAllSeedsHaveNonNullDefaultFormat() {
        for (com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds messageSeed : com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.values()) {
            assertThat(messageSeed.getDefaultFormat()).as(messageSeed.name() + " has null default format").isNotNull();
        }
    }

    @Test
    public void testDefaultFormatDoesntStartOrEndWithANonPrintableChar() {
        for (com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds messageSeed : com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds.values()) {
            String defaultFormat = messageSeed.getDefaultFormat();
            assertThat(defaultFormat.trim().length()).as(messageSeed.name() + " has empty default format").isNotZero()
                    .as(messageSeed.name() + " default format should not start or end with a non-printable character")
                    .isEqualTo(defaultFormat.length());
        }
    }

    @Test
    public void testAllMessageSeedDefaultFormatsAreWithinLengthLimit() {
        for (com.energyict.mdc.sap.soap.webservices.impl.MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(messageSeed.getDefaultFormat().length())
                    .as(messageSeed.name() + " default format is longer than max of 256").isLessThanOrEqualTo(256);
        }
    }
}
