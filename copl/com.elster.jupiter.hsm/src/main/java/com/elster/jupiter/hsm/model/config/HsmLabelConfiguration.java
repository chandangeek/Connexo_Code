/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.config;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public class HsmLabelConfiguration {

    public static final int IMPORT_FILE_LABEL = 0;
    public static final int IMPORT_LABEL_POSITION = 1;
    public static final int IMPORT_SESSION_CAPABILITY_POSITION = 2;
    public static final int DEVICE_KEY_LENGTH_POSITION = 3;
    public static final int RENEW_CAPABILITY_POSITION = 4;
    public static final int NO_OF_POSITIONS = 5;


    private final String importFileLabel;
    private final SessionKeyCapability importSessionKeyCapability;
    private final Integer deviceKeyLength;
    private final SessionKeyCapability renewSessionKeyCapability;
    private final String importLabel;

    public HsmLabelConfiguration(String importFileLabel, SessionKeyCapability importSessionKeyCapability, int deviceKeyLength, SessionKeyCapability renewSessionKeyCapability, String importLabel) {
        this.importFileLabel = importFileLabel;
        this.importSessionKeyCapability = importSessionKeyCapability;
        this.deviceKeyLength = deviceKeyLength;
        this.renewSessionKeyCapability = renewSessionKeyCapability;
        this.importLabel = importLabel;
    }

    public HsmLabelConfiguration(@Nonnull String value) throws HsmBaseException {
        String[] split = value.split(",", -1);

        try {
            this.importFileLabel = initString(split[IMPORT_FILE_LABEL].trim());
            String importLabel = split[IMPORT_LABEL_POSITION].trim();
            this.importLabel = initString(importLabel);
            String importSessionCapability = split[IMPORT_SESSION_CAPABILITY_POSITION].trim();
            this.importSessionKeyCapability = importSessionCapability.isEmpty()? null: SessionKeyCapability.valueOf(importSessionCapability);
            String deviceKeyLength = split[DEVICE_KEY_LENGTH_POSITION].trim();
            this.deviceKeyLength = deviceKeyLength.isEmpty()? null: Integer.parseInt(deviceKeyLength);
            String renewSessionCapability = split.length == NO_OF_POSITIONS ? split[RENEW_CAPABILITY_POSITION].trim(): "";
            this.renewSessionKeyCapability = renewSessionCapability.isEmpty()? null: SessionKeyCapability.valueOf(renewSessionCapability);

        } catch (IllegalArgumentException e) {
            throw new HsmBaseException(e);
        } catch (IndexOutOfBoundsException e1) {
            throw new HsmBaseException("Wrong label configuration format, label configuration value:" + value);
        }

    }

    public HsmLabelConfiguration(List<String> values) {
        this.importFileLabel = values.get(IMPORT_FILE_LABEL);
        this.importLabel = values.get(IMPORT_LABEL_POSITION);
        this.deviceKeyLength = Integer.parseInt(values.get(DEVICE_KEY_LENGTH_POSITION));
        this.importSessionKeyCapability = getImportSessionCapability(values.get(IMPORT_SESSION_CAPABILITY_POSITION));
        this.renewSessionKeyCapability = getImportSessionCapability(values.get(RENEW_CAPABILITY_POSITION));
    }


    /**
     * @return importFileLabel as present in import file
     * @throws HsmBaseException if not configured
     */
    public String getImportFileLabel() throws HsmBaseException {
        return checkNullAndReturn(importFileLabel, "Asking for import importFileLabel but not configured");
    }

    /**
     * @return session capability to be used during import phase
     * @throws HsmBaseException if not configured
     */
    public SessionKeyCapability getImportSessionCapability() throws HsmBaseException {
        return checkNullAndReturn(this.importSessionKeyCapability, "Asking for missing import capability");
    }

    /**
     * @return session capability to be used during renew phase
     * @throws HsmBaseException if not configured
     */
    public SessionKeyCapability getRenewSessionKeyCapability() throws HsmBaseException {
        return checkNullAndReturn(renewSessionKeyCapability, "Asking for missing renew capability");
    }

    /**
     * @return importFileLabel to be used for key storage/encryption
     * @throws HsmBaseException if not configured
     */
    public String getImportLabel() throws HsmBaseException {
        return checkNullAndReturn(importLabel, "Asking for re-encrypt importFileLabel but not configured");
    }

    public Integer getDeviceKeyLength() throws HsmBaseException {
        return checkNullAndReturn(deviceKeyLength, "Asking for key length but not configured");
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

    private SessionKeyCapability getImportSessionCapability(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return SessionKeyCapability.valueOf(value);
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
        return Objects.equals(importFileLabel, that.importFileLabel) &&
                importSessionKeyCapability == that.importSessionKeyCapability &&
                Objects.equals(deviceKeyLength, that.deviceKeyLength) &&
                renewSessionKeyCapability == that.renewSessionKeyCapability &&
                Objects.equals(importLabel, that.importLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(importFileLabel, importSessionKeyCapability, deviceKeyLength, renewSessionKeyCapability, importLabel);
    }

    @Override
    public String toString() {
        return "HsmLabelConfiguration{" +
                "importFileLabel='" + importFileLabel + '\'' +
                ", importSessionKeyCapability=" + importSessionKeyCapability +
                ", deviceKeyLength=" + deviceKeyLength +
                ", renewSessionKeyCapability=" + renewSessionKeyCapability +
                ", importLabel='" + importLabel + '\'' +
                '}';
    }
}
