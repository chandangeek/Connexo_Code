/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.nls.TranslationKey;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that all {@link TranslationKey}s
 * that are defined in this bundle are acceptable and unique.
 */
public class AllTranslationKeysTest {

    @Test
    public void testTranslationKeys() {
        Set<String> uniqueKeys = new HashSet<>();
        for (TranslationKey entry : new UsagePointDataModelServiceImpl().getKeys()) {
            String key = entry.getKey();
            String translation = "Translation key " + entry.getClass().getName() + '#' + key;
            assertThat(key).as(translation + " has null key")
                    .isNotNull()
                    .as(translation + " has empty key")
                    .isNotEmpty();
            assertThat(key.length())
                    .as(translation + " key should not start or end with a non-printable character")
                    .isEqualTo(key.trim().length())
                    .as(translation + " key is longer than max of 256")
                    .isLessThanOrEqualTo(256);
            assertThat(uniqueKeys.add(key)).as(translation + " does not have a unique key")
                    .isEqualTo(true);
            String defaultFormat = entry.getDefaultFormat();
            assertThat(defaultFormat).as(translation + " has null default format")
                    .isNotNull()
                    .as(translation + " has empty default format")
                    .isNotEmpty();
            assertThat(defaultFormat.length())
                    .as(translation + " default format should not start or end with a non-printable character")
                    .isEqualTo(defaultFormat.trim().length());
        }
    }
}
