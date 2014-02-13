package com.energyict.mdc.device.config.exceptions;

import org.junit.*;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link MessageSeeds} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-02-12 (13:36)
 */
public class MessageSeedsTest {

    @Test
    public void testAllMessageSeedsHaveUniqueId () {
        Set<Integer> uniqueIds = new HashSet<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            assertThat(uniqueIds).as(messageSeed.name() + " does not have a unique number").
                    doesNotContain(messageSeed.getNumber());
            uniqueIds.add(messageSeed.getNumber());
        }
    }

}