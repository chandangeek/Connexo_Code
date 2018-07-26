/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.model.configuration;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;

import javax.annotation.Nonnull;

public class HsmLabelConfiguration {

    private final String label;
    private final SessionKeyCapability importSessionKeyCapability;
    private final Integer keyLength;
    private final SessionKeyCapability renewSessionKeyCapability;
    private final String importReEncryptHsmLabel;

    public HsmLabelConfiguration(String label, SessionKeyCapability importSessionKeyCapability, int keyLength, SessionKeyCapability renewSessionKeyCapability, String importReEncryptHsmLabel) {
        this.label = label;
        this.importSessionKeyCapability = importSessionKeyCapability;
        this.keyLength = keyLength;
        this.renewSessionKeyCapability = renewSessionKeyCapability;
        this.importReEncryptHsmLabel = importReEncryptHsmLabel;
    }

    public HsmLabelConfiguration(@Nonnull String value) throws HsmBaseException {
        String[] split = value.split(",");

        try {
            this.label = initString(split[0].trim());
            String importKeyExtracted = split[1].trim();
            this.importSessionKeyCapability = importKeyExtracted.isEmpty()? null: SessionKeyCapability.valueOf(importKeyExtracted);
            String importKeyLength = split[2].trim();
            this.keyLength = importKeyLength.isEmpty()? null: Integer.parseInt(importKeyLength);
            String renewKeyExtracted = split[3].trim();
            this.renewSessionKeyCapability = renewKeyExtracted.isEmpty()? null: SessionKeyCapability.valueOf(renewKeyExtracted);
            String reEncryptImportLabel = split.length == 5? split[4].trim(): "";
            this.importReEncryptHsmLabel = initString(reEncryptImportLabel);
        } catch (IllegalArgumentException e) {
            throw new HsmBaseException(e);
        } catch (IndexOutOfBoundsException e1) {
            throw new HsmBaseException("Wrong label configuration format, label configuration value:" + value);
        }

    }

    /**
     * @return label as present in import file
     * @throws HsmBaseException if not configured
     */
    public String getFileImportLabel() throws HsmBaseException {
        return checkNullAndReturn(label, "Asking for import label but not configured");
    }

    /**
     * @return session capability to be used during import phase
     * @throws HsmBaseException if not configured
     */
    public SessionKeyCapability getImportSessionKeyCapability() throws HsmBaseException {
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
     * @return label to be used for key storage/encryption
     * @throws HsmBaseException if not configured
     */
    public String getImportLabel() throws HsmBaseException {
        return checkNullAndReturn(importReEncryptHsmLabel, "Asking for re-encrypt label but not configured");
    }

    public Integer getKeyLength() throws HsmBaseException {
        return checkNullAndReturn(keyLength, "Asking for key length but not configured");
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



}
