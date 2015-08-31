package com.energyict.mdc.engine.impl;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the IDs of all MessageSeed enum values in this engine bundle classes are unique.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-27 (13:49)
 */
public class AllMessageSeedsTest {

    @Test
    public void testAllMessageSeedsHaveUniqueId () {
        Set<Integer> uniqueIds = new HashSet<>();
        for (MessageSeed messageSeed : allMessageSeeds()) {
            assertThat(uniqueIds)
                    .as(messageSeed.toString() + " does not have a unique number")
                    .doesNotContain(messageSeed.getNumber());
            uniqueIds.add(messageSeed.getNumber());
        }
    }

    private List<MessageSeed> allMessageSeeds() {
        List<MessageSeed> all = new ArrayList<>();
        Collections.addAll(all, com.energyict.mdc.engine.exceptions.MessageSeeds.values());
        Collections.addAll(all, com.energyict.mdc.engine.impl.commands.store.MessageSeeds.values());
        Collections.addAll(all, com.energyict.mdc.engine.impl.monitor.MessageSeeds.values());
        return all;
    }

}