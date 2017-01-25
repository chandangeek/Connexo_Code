package com.energyict.mdc.device.topology.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link MessageSeeds} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-05 (12:26)
 */
public class MessageSeedsTest {

    private static final int MAX_KEY_LENGTH = 256;

    @Test
    public void testAllMessageSeedsHaveUniqueId () {
        Set<Integer> uniqueIds = new HashSet<>();
        for (com.energyict.mdc.device.data.impl.MessageSeeds messageSeed : com.energyict.mdc.device.data.impl.MessageSeeds.values()) {
            assertThat(uniqueIds).as(messageSeed.name() + " does not have a unique number").
                    doesNotContain(messageSeed.getNumber());
            uniqueIds.add(messageSeed.getNumber());
        }
    }

    @Test
    public void testAllMessageSeedsHaveUniqueKeys () {
        Set<String> uniqueKeys = new HashSet<>();
        for (com.energyict.mdc.device.data.impl.MessageSeeds messageSeed : com.energyict.mdc.device.data.impl.MessageSeeds.values()) {
            assertThat(uniqueKeys).as(messageSeed.name() + " does not have a unique key").
                    doesNotContain(messageSeed.getKey());
            uniqueKeys.add(messageSeed.getKey());
        }
    }

    @Test
    public void testAllMessageSeedKeysAreWithinLengthLimit () {
        for (com.energyict.mdc.device.data.impl.MessageSeeds messageSeed : com.energyict.mdc.device.data.impl.MessageSeeds.values()) {
            assertThat(messageSeed.getKey().length()).as(messageSeed.name() + " key is longer than max of 256").
                    isLessThanOrEqualTo(MAX_KEY_LENGTH);
        }
    }

}