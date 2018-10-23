/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.config;

import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.Objects;

public class HsmLabelConfiguration {

    public static final int IMPORT_FILE_LABEL_POSITION = 0;

    private final String label;
    private final String importFileLabel;

    public HsmLabelConfiguration(String label, String configuredStringValue) throws HsmBaseException {
        this.label = label;
        if (Objects.isNull(label) || label.trim().isEmpty() || Objects.isNull(configuredStringValue) || configuredStringValue.trim().isEmpty()) {
            throw new HsmBaseException("Wrong label configuration format, label configuration value:" + configuredStringValue);
        }
        String[] split = configuredStringValue.split(",", -1);
        try {
            this.importFileLabel = initString(split[IMPORT_FILE_LABEL_POSITION].trim());
        } catch (IllegalArgumentException e) {
            throw new HsmBaseException(e);
        } catch (IndexOutOfBoundsException e1) {
            throw new HsmBaseException("Wrong label configuration format, label configuration configuredStringValue:" + configuredStringValue);
        }

    }

    /**
     * @return importFileLabel as present in import file
     * @throws HsmBaseException if not configured
     */
    public String getImportFileLabel() throws HsmBaseException {
        return checkNullAndReturn(importFileLabel, "Asking for import importFileLabel but not configured");
    }

    public String getName() { return label;  }


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
        return Objects.equals(label, that.label) &&
                Objects.equals(importFileLabel, that.importFileLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, importFileLabel);
    }

    @Override
    public String toString() {
        return "HsmLabelConfiguration{" +
                "label='" + label + '\'' +
                ", importFileLabel='" + importFileLabel + '\'' +
                '}';
    }
}
