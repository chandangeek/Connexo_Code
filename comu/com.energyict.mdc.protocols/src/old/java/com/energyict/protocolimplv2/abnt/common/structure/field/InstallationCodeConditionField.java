/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common.structure.field;

import com.energyict.protocolimplv2.abnt.common.exception.ParsingException;
import com.energyict.protocolimplv2.abnt.common.field.AbstractField;

/**
 * @author sva
 * @since 14/08/2014 - 13:26
 */
public class InstallationCodeConditionField extends AbstractField<InstallationCodeConditionField> {

    public static final int LENGTH = 1;

    private int installationCode;
    private InstallationCodeCondition installationCodeCondition;

    public InstallationCodeConditionField() {
        this.installationCodeCondition = InstallationCodeCondition.UNKNOWN;
    }

    public InstallationCodeConditionField(InstallationCodeCondition installationCodeCondition) {
        this.installationCodeCondition = installationCodeCondition;
        this.installationCode = installationCodeCondition.getInstallationCode();
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(installationCode, LENGTH);
    }

    @Override
    public InstallationCodeConditionField parse(byte[] rawData, int offset) throws ParsingException {
        installationCode = (char) getIntFromBytes(rawData, offset, LENGTH);
        installationCodeCondition = InstallationCodeCondition.fromInstallationCode(installationCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getInstallationCode() {
        return installationCode;
    }

    public String getInstallationCodeMessage() {
        if (!this.installationCodeCondition.equals(InstallationCodeCondition.UNKNOWN)) {
            return installationCodeCondition.getMessage();
        } else {
            return (installationCodeCondition.getMessage() + " " + installationCode);
        }
    }

    public InstallationCodeCondition getInstallationCodeCondition() {
        return installationCodeCondition;
    }

    public enum InstallationCodeCondition {
        NOT_ACTIVE(0, "Does not activate nor deactivate"),
        ACTIVE(1, "Activated on response"),
        DEACTIVE(1, "Deactivated on response"),
        UNKNOWN(-1, "Unknown installation code");

        private final int installationCode;
        private final String message;

        private InstallationCodeCondition(int installationCode, String message) {
            this.installationCode = installationCode;
            this.message = message;
        }

        public int getInstallationCode() {
            return installationCode;
        }

        public String getMessage() {
            return message;
        }

        public static InstallationCodeCondition fromInstallationCode(int statusCode) {
            for (InstallationCodeCondition version : InstallationCodeCondition.values()) {
                if (version.getInstallationCode() == statusCode) {
                    return version;
                }
            }
            return InstallationCodeCondition.UNKNOWN;
        }
    }
}