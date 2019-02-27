/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.config;

import com.elster.jupiter.hsm.model.HsmBaseException;

import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;

import java.util.Objects;

public class HsmLabelConfiguration {

    public static final int IMPORT_FILE_LABEL_POSITION = 0;
    public static final int CHAINING_MODE_POSITION = 1;
    public static final int PADDING_ALGORITHM_POSITION = 2;

    private final String label;
    private final String importFileLabel;

    private final ChainingMode chainingMode;
    private final PaddingAlgorithm paddingAlgorithm;

    public HsmLabelConfiguration(String label, String configuredStringValue) throws HsmBaseException {
        this.label = label;
        if (Objects.isNull(label) || label.trim().isEmpty() || Objects.isNull(configuredStringValue) || configuredStringValue.trim().isEmpty()) {
            throw new HsmBaseException("Wrong label configuration format, label configuration value:" + configuredStringValue);
        }
        String[] split = configuredStringValue.split(",", -1);
        try {
            this.importFileLabel = initString(split[IMPORT_FILE_LABEL_POSITION]);
            this.chainingMode =  ChainingMode.valueOf(initString(split[CHAINING_MODE_POSITION]));
            this.paddingAlgorithm = PaddingAlgorithm.valueOf(initString(split[PADDING_ALGORITHM_POSITION]));
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

    public ChainingMode getChainingMode() {
        return chainingMode;
    }

    public PaddingAlgorithm getPaddingAlgorithm() {
        return paddingAlgorithm;
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

        if (label != null ? !label.equals(that.label) : that.label != null) {
            return false;
        }
        if (importFileLabel != null ? !importFileLabel.equals(that.importFileLabel) : that.importFileLabel != null) {
            return false;
        }
        if (chainingMode != that.chainingMode) {
            return false;
        }
        return paddingAlgorithm != null ? paddingAlgorithm.equals(that.paddingAlgorithm) : that.paddingAlgorithm == null;
    }

    @Override
    public int hashCode() {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (importFileLabel != null ? importFileLabel.hashCode() : 0);
        result = 31 * result + (chainingMode != null ? chainingMode.hashCode() : 0);
        result = 31 * result + (paddingAlgorithm != null ? paddingAlgorithm.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HsmLabelConfiguration{" +
                "label='" + label + '\'' +
                ", importFileLabel='" + importFileLabel + '\'' +
                ", chainingMode=" + chainingMode +
                ", paddingAlgorithm=" + paddingAlgorithm +
                '}';
    }
}
