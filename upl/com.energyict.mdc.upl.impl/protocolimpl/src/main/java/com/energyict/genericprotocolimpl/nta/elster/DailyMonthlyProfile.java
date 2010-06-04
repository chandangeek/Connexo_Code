package com.energyict.genericprotocolimpl.nta.elster;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.Calendar;

/**
 * Copyrights EnergyICT
 * Date: 2-jun-2010
 * Time: 17:08:25
 */
public class DailyMonthlyProfile extends com.energyict.genericprotocolimpl.nta.profiles.DailyMonthlyProfile {

    /**
     * Static ObisCode for the Status
     */
    private final static ObisCode OBISCODE_STATUS = ObisCode.fromString("0.0.96.10.2.255");

    /**
     * Constructor with the subClass
     *
     * @param am100 The SubClassed NTA protocol
     */
    public DailyMonthlyProfile(AM100 am100) {
        super(am100);
    }

    @Override
    protected void buildProfileData(final DataContainer dc, final ProfileData pd, final ProfileGeneric pg, final int timeDuration) throws IOException {

        try {
            Calendar cal = null;
            IntervalData currentInterval = null;
            int profileStatus = 0;
            if (dc.getRoot().getElements().length != 0) {
                for (int i = 0; i < dc.getRoot().getElements().length; i++) {

                    if (dc.getRoot().getStructure(i).isOctetString(0)) {
//						cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(getTimeZone());
//                        cal = new AXDRDateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
                        cal = dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).toCalendar(webrtu.getTimeZone());
                    } else {
                        if (cal != null) {
                            if (timeDuration == TimeDuration.DAYS) {
                                cal.add(Calendar.DAY_OF_MONTH, 1);
                            } else if (timeDuration == TimeDuration.MONTHS) {
                                cal.add(Calendar.MONTH, 1);
                            } else {
                                throw new ApplicationException("TimeDuration is not correct.");
                            }
                        }
                    }
                    if (getProfileStatusChannelIndex(pg) != -1) {
                        profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
                    } else {
                        profileStatus = 0;
                    }

                    if (cal != null) {
                        currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
                        if (currentInterval != null) {
                            pd.addInterval(currentInterval);
                        }
                    }
                }
            } else {
                webrtu.getLogger().info("No entries in LoadProfile");
            }
        } catch (final ClassCastException e) {
            e.printStackTrace();
            throw new ClassCastException("Configuration of the profile probably not correct.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getProfileStatusChannelIndex(final ProfileGeneric pg) throws IOException {
        try {
            for (int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++) {
                if (isProfileStatusObisCode(((CapturedObject) (pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())) {
                    return i;
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
            throw new IOException("Could not retrieve the index of the profileData's status attribute.");
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean isProfileStatusObisCode(final ObisCode oc) throws IOException {
        return OBISCODE_STATUS.equals(oc);
    }

}
