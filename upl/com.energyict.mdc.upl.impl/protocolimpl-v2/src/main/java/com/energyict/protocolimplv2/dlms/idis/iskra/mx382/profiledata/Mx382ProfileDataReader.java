package com.energyict.protocolimplv2.dlms.idis.iskra.mx382.profiledata;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.profiledata.IDISProfileDataReader;

/**
 * Created by cisac on 1/19/2016.
 */
public class Mx382ProfileDataReader extends IDISProfileDataReader {

    private static final ObisCode QUARTER_HOURLY_MAXDEMAND_LOAD_PROFILE = ObisCode.fromString("0.0.98.1.0.255");
    private static final ObisCode DAILY_MAXDEMAND_LOAD_PROFILE = ObisCode.fromString("0.0.98.2.0.255");
    private final int PROFILE_STATUS_DEVICE_DISTURBANCE = 0x01;
    private final int PROFILE_STATUS_RESET_CUMULATION = 0x10;
    private final int PROFILE_STATUS_DEVICE_CLOCK_CHANGED = 0x20;
    private final int PROFILE_STATUS_POWER_RETURNED = 0x40;
    private final int PROFILE_STATUS_POWER_FAILURE = 0x80;

    public Mx382ProfileDataReader(AbstractDlmsProtocol protocol, long limitMaxNrOfDays) {
        super(protocol, limitMaxNrOfDays);
        supportedLoadProfiles.add(QUARTER_HOURLY_MAXDEMAND_LOAD_PROFILE);
        supportedLoadProfiles.add(DAILY_MAXDEMAND_LOAD_PROFILE);
    }

    @Override
    protected int getEiServerStatus(int protocolStatus) {
        int status = IntervalStateBits.OK;

        if ((protocolStatus & PROFILE_STATUS_DEVICE_DISTURBANCE) == PROFILE_STATUS_DEVICE_DISTURBANCE) {
            status |= IntervalStateBits.DEVICE_ERROR;
        }
        if ((protocolStatus & PROFILE_STATUS_RESET_CUMULATION) == PROFILE_STATUS_RESET_CUMULATION) {
            status |= IntervalStateBits.OTHER;
        }
        if ((protocolStatus & PROFILE_STATUS_DEVICE_CLOCK_CHANGED) == PROFILE_STATUS_DEVICE_CLOCK_CHANGED) {
            status |= IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & PROFILE_STATUS_POWER_RETURNED) == PROFILE_STATUS_POWER_RETURNED) {
            status |= IntervalStateBits.POWERUP;
        }
        if ((protocolStatus & PROFILE_STATUS_POWER_FAILURE) == PROFILE_STATUS_POWER_FAILURE) {
            status |= IntervalStateBits.POWERDOWN;
        }

        return status;
    }

    @Override
    protected boolean hasStatusInformation(ObisCode correctedLoadProfileObisCode) {
        return false;
    }
}
