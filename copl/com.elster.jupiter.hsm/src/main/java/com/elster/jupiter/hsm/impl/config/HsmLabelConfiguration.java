/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl.config;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class HsmLabelConfiguration {

    public static final int IMPORT_FILE_LABEL = 0;


    private final String importFileLabel;

    public HsmLabelConfiguration(@Nonnull String value) throws HsmBaseException {
        if (Objects.isNull(value) || value.trim().isEmpty()) {
            throw new HsmBaseException("Wrong label configuration format, label configuration value:" + value);
        }
        String[] split = value.split(",", -1);

        try {
            this.importFileLabel = initString(split[IMPORT_FILE_LABEL].trim());
        } catch (IllegalArgumentException e) {
            throw new HsmBaseException(e);
        } catch (IndexOutOfBoundsException e1) {
            throw new HsmBaseException("Wrong label configuration format, label configuration value:" + value);
        }

    }

    public HsmLabelConfiguration(List<String> values) {
        this.importFileLabel = values.get(IMPORT_FILE_LABEL);
    }


    /**
     * @return importFileLabel as present in import file
     * @throws HsmBaseException if not configured
     */
    public String getImportFileLabel() throws HsmBaseException {
        return checkNullAndReturn(importFileLabel, "Asking for import importFileLabel but not configured");
    }


    private <T extends Object> T checkNullAndReturn(T obj, String msg) throws HsmBaseException {
        if (obj == null) {
            throw new HsmBaseException(msg);
        }
        return obj;
    }

    private String initString(String s) {
        if (s != null  && !s.isEmpty()) {
            return s.trim();
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HsmLabelConfiguration)) {
            return false;
        }
        HsmLabelConfiguration that = (HsmLabelConfiguration) o;
        return Objects.equals(importFileLabel, that.importFileLabel);
    }

    @Override
    public int hashCode() {

        return Objects.hash(importFileLabel);
    }

    @Override
    public String toString() {
        return "HsmLabelConfiguration{" +
                "importFileLabel='" + importFileLabel + '\'' +
                '}';
    }
}
