/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.configuration;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.KeyType;

import javax.annotation.Nonnull;

public class HsmLabelConfiguration {

    private final String label;
    private final KeyType importKeyType;
    private final KeyType renewKeyType;
    private final String importReEncryptHsmLabel;

    public HsmLabelConfiguration(String label, KeyType importKeyType, KeyType renewKeyType, String importReEncryptHsmLabel) {
        this.label = label;
        this.importKeyType = importKeyType;
        this.renewKeyType = renewKeyType;
        this.importReEncryptHsmLabel = importReEncryptHsmLabel;
    }

    public HsmLabelConfiguration(@Nonnull String value) throws HsmBaseException {
        String[] split = value.split(",");

        try {
            this.label = initString(split[0]);
            String importKeyExtracted = split[1].trim();
            this.importKeyType = importKeyExtracted.isEmpty()? null:KeyType.valueOf(importKeyExtracted);
            String renewKeyExtracted = split[2].trim();
            this.renewKeyType = renewKeyExtracted.isEmpty()? null:KeyType.valueOf(renewKeyExtracted);
            String reEncryptImportLabel = split.length == 4? split[3].trim(): "";
            this.importReEncryptHsmLabel = initString(reEncryptImportLabel);
        } catch (IllegalArgumentException e) {
            throw new HsmBaseException(e);
        } catch (IndexOutOfBoundsException e1) {
            throw new HsmBaseException("Wrong label configuration format, label configuration value:" + value);
        }

    }

    private String initString(String s) {
        if (s != null  && !s.isEmpty()) {
            return s.trim();
        }
        return null;
    }

    public String getFileImportLabel() throws HsmBaseException {
        return checkNullAndReturn(label, "Asking for import label but not configured");
    }

    public KeyType getImportKeyType() throws HsmBaseException {
        return checkNullAndReturn(this.importKeyType, "Asking for missing import capability");
    }

    public KeyType getRenewKeyType() throws HsmBaseException {
        return checkNullAndReturn(renewKeyType, "Asking for missing renew capability");
    }

    public String getImportReEncryptHsmLabel() throws HsmBaseException {
        return checkNullAndReturn(importReEncryptHsmLabel, "Asking for re-encrypt label but not configured");
    }

    private <T extends Object> T checkNullAndReturn(T obj, String msg) throws HsmBaseException {
        if (obj == null) {
            throw new HsmBaseException(msg);
        }
        return obj;
    }
}
