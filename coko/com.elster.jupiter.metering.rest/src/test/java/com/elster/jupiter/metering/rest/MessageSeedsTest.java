package com.elster.jupiter.metering.rest;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.rest.impl.MessageSeeds;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MessageSeedsTest {

    @Test
    public void testAllMessageSeedsHaveUniqueId() {
        Set<Integer> uniqueIds = new HashSet<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(uniqueIds).as(messageSeed.name() + " does not have a unique number").
                    doesNotContain(messageSeed.getNumber());
            uniqueIds.add(messageSeed.getNumber());
        }
    }

    @Test
    public void testAllMessageSeedsHaveUniqueKeys() {
        Set<String> uniqueKeys = new HashSet<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(uniqueKeys).as(messageSeed.name() + " does not have a unique key").
                    doesNotContain(messageSeed.getKey());
            uniqueKeys.add(messageSeed.getKey());
        }
    }

    @Test
    public void testAllMessageSeedKeysAreWithinLengthLimit() {
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(messageSeed.getKey().length()).as(messageSeed.name() + " key is longer than max of 256").
                    isLessThanOrEqualTo(256);
        }
    }

    @Test
    public void testMessageSeedKeysDontContainComponentName() throws Exception {
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(messageSeed.getKey()).doesNotContain(MeteringService.COMPONENTNAME + ".");
        }
    }

}