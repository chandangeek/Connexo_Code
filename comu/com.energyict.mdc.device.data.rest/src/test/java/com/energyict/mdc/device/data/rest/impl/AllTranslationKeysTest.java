/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.TranslationKey;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static junit.framework.TestCase.fail;

/**
 * Tests that all {@link TranslationKey}s
 * that are defined in this bundle are unique.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (11:35)
 */
public class AllTranslationKeysTest {

    @Test
    public void testTranslationKeysAreUnique() throws Exception {
        Map<String, TranslationKey> uniqueKeys = new HashMap<>();
        for (TranslationKey translationKey : new DeviceApplication().getKeys()) {
            if (uniqueKeys.containsKey(translationKey.getKey())) {
                TranslationKey duplicate = uniqueKeys.get(translationKey.getKey());
                fail("Translation key:" + this.toString(translationKey) + " is a duplicate for " + this.toString(duplicate));
            } else {
                uniqueKeys.put(translationKey.getKey(), translationKey);
            }
        }
    }

    private String toString(TranslationKey translationKey) {
        return translationKey.getClass().getName() + "#" + translationKey.getKey();
    }

}