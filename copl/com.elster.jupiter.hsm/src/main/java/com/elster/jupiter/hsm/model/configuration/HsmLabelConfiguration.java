/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.configuration;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.KeyType;

public class HsmLabelConfiguration {

    private final String label;
    private final KeyType importKeyType;
    private final KeyType renewKeyType;

    public HsmLabelConfiguration(String label, KeyType importKeyType, KeyType renewKeyType) {
        this.label = label;
        this.importKeyType = importKeyType;
        this.renewKeyType = renewKeyType;
    }

    public HsmLabelConfiguration(String value) throws HsmBaseException {
        String[] split = value.split(",");
        if (split.length != 3) {
            throw new HsmBaseException("Wrong label configuration format");
        }
        this.label = split[0].trim();
        this.importKeyType = KeyType.valueOf(split[1].trim());
        this.renewKeyType = KeyType.valueOf(split[2].trim());

    }

    public String getFileImportLabel() {
        return label;
    }

    public KeyType getImportKeyType() {
        return importKeyType;
    }

    public KeyType getRenewKeyType() {
        return renewKeyType;
    }
}
