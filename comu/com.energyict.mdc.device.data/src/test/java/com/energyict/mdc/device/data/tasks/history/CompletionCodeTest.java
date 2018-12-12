/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.tasks.history;

import java.util.EnumSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link CompletionCode} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-15 (15:50)
 */
public class CompletionCodeTest {

    @Test
    public void testAlwaysUpgradeFromLowestPriority() {
        Set<CompletionCode> allButLowestPriority = EnumSet.complementOf(EnumSet.of(CompletionCode.Ok));
        for (CompletionCode notLowestPriority : allButLowestPriority) {
            assertThat(CompletionCode.Ok.upgradeTo(notLowestPriority)).isEqualTo(notLowestPriority);
        }
    }

    @Test
    public void testNeverUpgradeFromHighestPriority() {
        Set<CompletionCode> allButHighestPriority = EnumSet.complementOf(EnumSet.of(CompletionCode.ConnectionError));
        for (CompletionCode notHighestPriority : allButHighestPriority) {
            assertThat(CompletionCode.ConnectionError.upgradeTo(notHighestPriority)).isEqualTo(CompletionCode.ConnectionError);
        }
    }

    @Test
    public void testUpgradeToSame() {
        for (CompletionCode completionCode : CompletionCode.values()) {
            assertThat(completionCode.upgradeTo(completionCode)).
                    as("Upgrade to the same CompletionCode " + completionCode + " fails").
                    isEqualTo(completionCode);
        }
    }

    @Test
    public void testDBValues() {
        assertThat(CompletionCode.Ok).isEqualTo(CompletionCode.fromDBValue(0));
        assertThat(CompletionCode.ConnectionError).isEqualTo(CompletionCode.fromDBValue(8));
    }
}