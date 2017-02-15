/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class MeterInstallationStatusField extends AbstractField<MeterInstallationStatusField> {

    public static final int LENGTH = 1;

    private int installationStatusCode;
    private InstallationStatus installationStatus;

    public MeterInstallationStatusField() {
        this.installationStatus = InstallationStatus.UNKNOWN;
    }

    public MeterInstallationStatusField(InstallationStatus installationStatus) {
        this.installationStatus = installationStatus;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromInt(installationStatusCode, 1);
    }

    @Override
    public MeterInstallationStatusField parse(byte[] rawData, int offset) throws ParsingException {
        installationStatusCode = getIntFromBytesLE(rawData, offset, LENGTH);
        installationStatus = InstallationStatus.fromStatusCode(installationStatusCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getInstallationStatusCode() {
        return installationStatusCode;
    }

    public String getInstallationStatusInfo() {
        if (!this.installationStatus.equals(InstallationStatus.UNKNOWN)) {
            return installationStatus.getStatusInfo();
        } else {
            return (installationStatus.getStatusInfo() + " " + installationStatus);
        }
    }

    public InstallationStatus getInstallationStatus() {
        return installationStatus;
    }

    public enum InstallationStatus {
        NOT_CONFIGURED(0, "Not configured"),
        MONO_PHASE(1, "Mono-phase"),
        TWO_PHASE(2, "Two-phase"),
        THREE_PHASE(3, "Three-phase"),
        UNKNOWN(-1, "Unknown installation status");

        private final int statusCode;
        private final String statusInfo;

        private InstallationStatus(int statusCode, String statusInfo) {
            this.statusCode = statusCode;
            this.statusInfo = statusInfo;
        }

        public String getStatusInfo() {
            return statusInfo;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public static InstallationStatus fromStatusCode(int statusCode) {
            for (InstallationStatus version : InstallationStatus.values()) {
                if (version.getStatusCode() == statusCode) {
                    return version;
                }
            }
            return InstallationStatus.UNKNOWN;
        }
    }
}