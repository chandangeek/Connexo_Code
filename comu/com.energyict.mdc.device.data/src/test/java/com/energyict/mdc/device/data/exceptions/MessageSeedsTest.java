/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.exceptions;

import com.energyict.mdc.device.data.impl.MessageSeeds;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link MessageSeeds} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-06 (14:44)
 */
public class MessageSeedsTest {

    @Test
    public void testAllSeedsHaveUniqueNumber() {
        Set<Integer> uniqueIds = new HashSet<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(uniqueIds).as(messageSeed.name() + " does not have a unique number")
                    .doesNotContain(messageSeed.getNumber());
            uniqueIds.add(messageSeed.getNumber());
        }
    }

    @Test
    public void testAllMessageSeedsHaveUniqueKeys() {
        Set<String> uniqueKeys = new HashSet<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(uniqueKeys.add(messageSeed.getKey())).as(messageSeed.name() + " does not have a unique key")
                    .isEqualTo(true);
        }
    }

    @Test
    public void testAllSeedsHaveNonNullKey() {
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(messageSeed.getKey()).as(messageSeed.name() + " has null key")
                    .isNotNull();
        }
    }

    @Test
    public void testKeyDoesntStartOrEndWithANonPrintableChar() {
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            String key = messageSeed.getKey();
            assertThat(key.trim().length())
                    .as(messageSeed.name() + " has empty key")
                    .isNotZero()
                    .as(messageSeed.name() + " key should not start or end with a non-printable character")
                    .isEqualTo(key.length());
        }
    }

    @Test
    public void testAllMessageSeedKeysAreWithinLengthLimit() {
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(messageSeed.getKey().length()).as(messageSeed.name() + " key is longer than max of 256")
                    .isLessThanOrEqualTo(256);
        }
    }

    @Test
    public void testAllSeedsHaveNonNullDefaultFormat() {
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(messageSeed.getDefaultFormat()).as(messageSeed.name() + " has null default format")
                    .isNotNull();
        }
    }

    @Test
    public void testDefaultFormatDoesntStartOrEndWithANonPrintableChar() {
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            String defaultFormat = messageSeed.getDefaultFormat();
            assertThat(defaultFormat.trim().length())
                    .as(messageSeed.name() + " has empty default format")
                    .isNotZero()
                    .as(messageSeed.name() + " default format should not start or end with a non-printable character")
                    .isEqualTo(defaultFormat.length());
        }
    }

    @Test
    public void testAllMessageSeedDefaultFormatsAreWithinLengthLimit() {
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(messageSeed.getDefaultFormat().length()).as(messageSeed.name() + " default format is longer than max of 256")
                    .isLessThanOrEqualTo(256);
        }
    }
}
