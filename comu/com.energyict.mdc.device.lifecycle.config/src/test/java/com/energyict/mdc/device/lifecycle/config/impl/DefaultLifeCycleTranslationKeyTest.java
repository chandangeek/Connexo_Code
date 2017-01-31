/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.impl;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link DefaultLifeCycleTranslationKey} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-11 (11:05)
 */
public class DefaultLifeCycleTranslationKeyTest {

    @Test
    public void testAllMessageSeedsHaveUniqueKeys () {
        Set<String> uniqueKeys = new HashSet<>();
        for (DefaultLifeCycleTranslationKey translationKey : DefaultLifeCycleTranslationKey.values()) {
            assertThat(uniqueKeys)
                .as(translationKey.name() + " does not have a unique key")
                .doesNotContain(translationKey.getKey());
            uniqueKeys.add(translationKey.getKey());
        }
    }

    @Test
    public void testAllMessageSeedKeysAreWithinLengthLimit () {
        for (DefaultLifeCycleTranslationKey translationKey : DefaultLifeCycleTranslationKey.values()) {
            assertThat(translationKey.getKey().length())
                .as(translationKey.name() + " key is longer than max of 256")
                .isLessThanOrEqualTo(256);
        }
    }

}