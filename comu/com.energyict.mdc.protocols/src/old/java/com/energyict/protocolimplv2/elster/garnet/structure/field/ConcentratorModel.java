package com.energyict.protocolimplv2.elster.garnet.structure.field;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class ConcentratorModel extends AbstractField<ConcentratorModel> {

    public static final int LENGTH = 1;

    private int versionInfoCode;
    private VersionInfo versionInfo;

    public ConcentratorModel() {
        this.versionInfo = VersionInfo.UNKNOWN;
    }

    public ConcentratorModel(VersionInfo versionInfo) {
        this.versionInfo = versionInfo;
    }

    @Override
    public byte[] getBytes() {
        return getBytesFromIntLE(versionInfo.getVersionCode(), LENGTH);
    }

    @Override
    public ConcentratorModel parse(byte[] rawData, int offset) throws ParsingException {
        versionInfoCode = getIntFromBytesLE(rawData, offset, LENGTH);
        versionInfo = VersionInfo.fromVersionCode(versionInfoCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getVersionInfoCode() {
        return versionInfoCode;
    }

    public String getVersionInfo() {
        if (!this.versionInfo.equals(VersionInfo.UNKNOWN)) {
            return versionInfo.getVersionInfo();
        } else {
            return (versionInfo.getVersionInfo() + " " + versionInfo);
        }
    }

    private enum VersionInfo {
        CPU_OF_GARNET_CS_AND_CP_CAS(0, "CPU of Garnet CS and CP CAS"),
        CPU_OF_DRACON(2, "CPU of Dracon"),
        CPU_OF_GARNET_CS_ELSTER(7, "CPU of Garnet CS Elster"),
        CPU_OF_GARNET_CP_ELSTER(8, "CPU of Garnet CP Elster"),
        UNKNOWN(-1, "Unknown version");

        private final int versionCode;
        private final String versionInfo;

        private VersionInfo(int versionCode, String versionInfo) {
            this.versionCode = versionCode;
            this.versionInfo = versionInfo;
        }

        public String getVersionInfo() {
            return versionInfo;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public static VersionInfo fromVersionCode(int versionCode) {
            for (VersionInfo version : VersionInfo.values()) {
                if (version.getVersionCode() == versionCode) {
                    return version;
                }
            }
            return VersionInfo.UNKNOWN;
        }
    }
}