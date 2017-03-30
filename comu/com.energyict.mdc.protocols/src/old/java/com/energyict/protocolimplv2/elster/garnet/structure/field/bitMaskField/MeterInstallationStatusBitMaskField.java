/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class MeterInstallationStatusBitMaskField extends AbstractBitMaskField<MeterInstallationStatusBitMaskField> {

    public static final int LENGTH = 2; // The length expressed in nr of bits

    private BitSet installationStatusMask;
    private int installationStatusCode;
    private InstallationStatus installationStatus;

    public MeterInstallationStatusBitMaskField() {
        this.installationStatusMask = new BitSet(LENGTH);
        this.installationStatus = InstallationStatus.UNKNOWN;
    }

    public MeterInstallationStatusBitMaskField(InstallationStatus installationStatus) {
        this.installationStatus = installationStatus;
    }

    public BitSet getBitMask() {
        return installationStatusMask;
    }

    @Override
    public MeterInstallationStatusBitMaskField parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        installationStatusMask = bitSet.get(startPos, startPos + LENGTH);
        installationStatusCode = convertBitSetToInt(installationStatusMask);
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