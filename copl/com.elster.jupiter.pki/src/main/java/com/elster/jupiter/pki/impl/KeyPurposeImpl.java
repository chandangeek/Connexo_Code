/*
 * Copyright (c) 2020 by Honeywell Inc. All rights reserved.
 */
package com.elster.jupiter.pki.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.pki.KeyPurpose;

import java.util.stream.Stream;

public enum KeyPurposeImpl implements TranslationKey {

    AUTH("Authentication key"),
    ENCRYPTION("Encryption key"),
    PRE_SHARED("Pre-shared key"),
    PRE_SHARED_WRAPPER("Pre-shared key wrapping key"),
    FIRMWARE("Firmware upgrade authentication key"),
    COMMUNICATION("Communication keys wrapper key"),
    OTHER("Other");

    private String defaultFormat;

    KeyPurposeImpl(String defaultFormat) {
        this.defaultFormat = defaultFormat;
    }

    public static KeyPurposeImpl from(String key) {
        return Stream.of(KeyPurposeImpl.values())
                .filter(d -> d.name().equals(key))
                .findFirst().orElse(OTHER);
    }

    @Override
    public String getKey() {
        return super.name();
    }

    @Override
    public String getDefaultFormat() {
        return defaultFormat;
    }

    public KeyPurpose asKeyPurpose(Thesaurus thesaurus) {
        return new KeyPurpose() {
            @Override
            public String getId() {
                return name();
            }

            @Override
            public String getName() {
                return thesaurus.getFormat(KeyPurposeImpl.this).format();
            }
        };
    }
}
