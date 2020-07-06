/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;

import com.elster.jupiter.users.impl.UserServiceImpl;
import com.elster.jupiter.util.exception.MessageSeed;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MessageSeedsTest {
    private static final Collection<MessageSeed> MESSAGE_SEEDS = new UserServiceImpl().getSeeds();

    @Test
    public void testAllSeedsHaveUniqueNumber() {
        Set<Integer> uniqueIds = new HashSet<>();
        for (MessageSeed messageSeed : MESSAGE_SEEDS) {
            assertThat(uniqueIds).as(getName(messageSeed) + " does not have a unique number")
                    .doesNotContain(messageSeed.getNumber());
            uniqueIds.add(messageSeed.getNumber());
        }
    }

    @Test
    public void testAllMessageSeedsHaveUniqueKeys() {
        Set<String> uniqueKeys = new HashSet<>();
        for (MessageSeed messageSeed : MESSAGE_SEEDS) {
            assertThat(uniqueKeys.add(messageSeed.getKey())).as(getName(messageSeed) + " does not have a unique key")
                    .isEqualTo(true);
        }
    }

    @Test
    public void testAllSeedsHaveNonNullKey() {
        for (MessageSeed messageSeed : MESSAGE_SEEDS) {
            assertThat(messageSeed.getKey()).as(getName(messageSeed) + " has null key")
                    .isNotNull();
        }
    }

    @Test
    public void testKeyDoesntStartOrEndWithANonPrintableChar() {
        for (MessageSeed messageSeed : MESSAGE_SEEDS) {
            String key = messageSeed.getKey();
            assertThat(key.trim().length())
                    .as(getName(messageSeed) + " has empty key")
                    .isNotZero()
                    .as(getName(messageSeed) + " key should not start or end with a non-printable character")
                    .isEqualTo(key.length());
        }
    }

    @Test
    public void testAllMessageSeedKeysAreWithinLengthLimit() {
        for (MessageSeed messageSeed : MESSAGE_SEEDS) {
            assertThat(messageSeed.getKey().length()).as(getName(messageSeed) + " key is longer than max of 256")
                    .isLessThanOrEqualTo(256);
        }
    }

    @Test
    public void testAllSeedsHaveNonNullDefaultFormat() {
        for (MessageSeed messageSeed : MESSAGE_SEEDS) {
            assertThat(messageSeed.getDefaultFormat()).as(getName(messageSeed) + " has null default format")
                    .isNotNull();
        }
    }

    @Test
    public void testDefaultFormatDoesntStartOrEndWithANonPrintableChar() {
        for (MessageSeed messageSeed : MESSAGE_SEEDS) {
            String defaultFormat = messageSeed.getDefaultFormat();
            assertThat(defaultFormat.trim().length())
                    .as(getName(messageSeed) + " has empty default format")
                    .isNotZero()
                    .as(getName(messageSeed) + " default format should not start or end with a non-printable character")
                    .isEqualTo(defaultFormat.length());
        }
    }

    @Test
    public void testAllMessageSeedDefaultFormatsAreWithinLengthLimit() {
        for (MessageSeed messageSeed : MESSAGE_SEEDS) {
            assertThat(messageSeed.getDefaultFormat().length()).as(getName(messageSeed) + " default format is longer than max of 256")
                    .isLessThanOrEqualTo(256);
        }
    }

    private static String getName(Object value) {
        String name = MessageSeed.class.getSimpleName() + " in " + value.getClass().getName();
        if (value instanceof Enum) {
            name += '#' + ((Enum) value).name();
        }
        return name;
    }
}
