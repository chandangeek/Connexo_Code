package com.energyict.genericprotocolimpl.nta.profiles;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.TimeDuration;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.StatusCodeProfile;
import com.energyict.genericprotocolimpl.common.pooling.ChannelFullProtocolShadow;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gna
 */
public class MbusDailyMonthlyProfile extends AbstractNTAProfile {

    private AbstractMbusDevice mbusDevice;

    public MbusDailyMonthlyProfile() {
    }

    public MbusDailyMonthlyProfile(AbstractMbusDevice mbusDevice) {
        this.mbusDevice = mbusDevice;
    }

    public ProfileData getMonthlyProfile(ObisCode mbusProfile) throws IOException {
        ProfileData profileData = new ProfileData();
        ProfileGeneric genericProfile;

        genericProfile = getCosemObjectFactory().getProfileGeneric(mbusProfile);
        List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.MONTHS);

        if (channelInfos.size() != 0) {

            profileData.setChannelInfos(channelInfos);
            Calendar fromCalendar = null;
            Calendar channelCalendar = null;
            Calendar toCalendar = getToCalendar();

            if (this.mbusDevice.getWebRTU().isRequestOneDay()) {
                fromCalendar = ProtocolUtils.getCalendar(this.mbusDevice.getWebRTU().getTimeZone());
                fromCalendar.add(Calendar.HOUR, -24);
                this.mbusDevice.getLogger().log(Level.INFO, "Requesting One Day - from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
            } else {
                for (ChannelFullProtocolShadow channelFPS : this.mbusDevice.getFullShadow().getRtuShadow().getChannelFullProtocolShadow()) {
                 if (channelFPS.getTimeDuration().getTimeUnitCode() == TimeDuration.MONTHS) {
                        channelCalendar = getFromCalendar(channelFPS);
                     if ((fromCalendar == null) || (channelCalendar.before(fromCalendar))) {
                            fromCalendar = channelCalendar;
                        }
                    }
                }
                this.mbusDevice.getLogger().log(Level.INFO, "Reading Monthly values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
            }

            DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
            buildProfileData(dc, profileData, genericProfile, TimeDuration.MONTHS);
            ParseUtils.validateProfileData(profileData, toCalendar.getTime());
            ProfileData pd = sortOutProfiledate(profileData, TimeDuration.MONTHS);
            pd.sort();
			if(mbusDevice.getWebRTU().isBadTime()){
				pd.markIntervalsAsBadTime();
			}

            return pd;
        }
        return null;
    }

    private void buildProfileData(DataContainer dc, ProfileData pd, ProfileGeneric pg, int timeDuration) throws IOException {

        Calendar cal = null;
        IntervalData currentInterval = null;
        int profileStatus = 0;
        if (dc.getRoot().getElements().length != 0) {
            for (int i = 0; i < dc.getRoot().getElements().length; i++) {
                if (dc.getRoot().getStructure(i).isOctetString(0)) {
                    cal = new AXDRDateTime(new OctetString(dc.getRoot().getStructure(i).getOctetString(getProfileClockChannelIndex(pg)).getArray())).getValue();
                } else {
                    if (cal != null) {
                        if (timeDuration == TimeDuration.DAYS) { //the daily profile is constructed from the hourly profile so only add 1 hour to the calendar
                            cal.add(Calendar.DAY_OF_MONTH, 1);
                        } else if (timeDuration == TimeDuration.MONTHS) {
                            cal.add(Calendar.MONTH, 1);
                        } else {
                            throw new ApplicationException("TimeDuration is not correct.");
                        }
                    }
                }
                if (cal != null) {

                    if (getProfileStatusChannelIndex(pg) != -1) {
                        profileStatus = dc.getRoot().getStructure(i).getInteger(getProfileStatusChannelIndex(pg));
                    } else {
                        profileStatus = 0;
                    }

                    currentInterval = getIntervalData(dc.getRoot().getStructure(i), cal, profileStatus, pg, pd.getChannelInfos());
                    if (currentInterval != null) {
                        pd.addInterval(currentInterval);
                    }
                }
            }
        } else {
            this.mbusDevice.getLogger().info("No entries in LoadProfile");
        }
    }

    private int getProfileStatusChannelIndex(ProfileGeneric pg) throws IOException {
        try {
            for (int i = 0; i < pg.getCaptureObjectsAsUniversalObjects().length; i++) {
                if (((CapturedObject) (pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getMbusStatusObject(this.mbusDevice.getPhysicalAddress()).getObisCode())) {
                    return i;
                }
            }
        } catch (IOException e) {
            throw new IOException("Could not retrieve the index of the profileData's status attribute.");
        }
        return -1;
    }

    private int getProfileClockChannelIndex(ProfileGeneric pg) throws IOException {
        try {
            for (int i = 0; i < pg.getCaptureObjects().size(); i++) {
                if (((CapturedObject) (pg.getCaptureObjects().get(i))).getLogicalName().getObisCode().equals(getMeterConfig().getClockObject().getObisCode())) {
                    return i;
                }
            }
        } catch (IOException e) {
            throw new IOException("Could not retrieve the index of the profileData's clock attribute.");
        }
        return -1;
    }

    private IntervalData getIntervalData(DataStructure ds, Calendar cal, int status, ProfileGeneric pg, List channelInfos) throws IOException {

        IntervalData id = new IntervalData(cal.getTime(), StatusCodeProfile.intervalStateBits(status));
        int index = 0;

        try {
            for (int i = 0; i < pg.getCaptureObjects().size(); i++) {
                if (index < channelInfos.size()) {
                    if (isMbusRegisterObisCode(((CapturedObject) (pg.getCaptureObjects().get(i))).getLogicalName().getObisCode())) {
                        id.addValue(new Integer(ds.getInteger(i)));
                        index++;
                    }
                }
            }
        } catch (IOException e) {
            throw new IOException("Failed to parse the intervalData objects form the datacontainer.");
        }

        return id;
    }

    private List<ChannelInfo> getDailyMonthlyChannelInfos(ProfileGeneric profile, int timeDuration) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        ChannelInfo ci = null;
        int index = 0;
        int channelIndex = -1;
        try {
            for (int i = 0; i < profile.getCaptureObjects().size(); i++) {

                if (isMbusRegisterObisCode(((CapturedObject) (profile.getCaptureObjects().get(i))).getLogicalName().getObisCode())) { // make a channel out of it
                    CapturedObject co = ((CapturedObject) profile.getCaptureObjects().get(i));
                    ScalerUnit su = getMeterDemandRegisterScalerUnit(co.getLogicalName().getObisCode());

                    channelIndex = getDMChannelNumber(index + 1, timeDuration);

                    if (channelIndex != -1) {
                        if ((su != null) && (su.getUnitCode() != 0)) {
                            ci = new ChannelInfo(index, channelIndex, "WebRtuKP_Mbus_DayMonth_" + index, su.getEisUnit());
                        } else {
                            throw new ProtocolException("Meter does not report a proper scalerUnit for all channels of his Mbus DailyMonthly LoadProfile, data can not be interpreted correctly.");
                        }
                        index++;
                        //TODO need to check the wrapValue
                        ci.setCumulativeWrapValue(BigDecimal.valueOf(1).movePointRight(9));
                        channelInfos.add(ci);
                    }

                }

            }
        } catch (IOException e) {
            throw new IOException("Failed to build the channelInfos." + e);
        }
        return channelInfos;
    }

    private int getDMChannelNumber(int index, int duration) {
        int channelIndex = 0;
        for (int i = 0; i < this.mbusDevice.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().size(); i++) {
            if (this.mbusDevice.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(i).getTimeDuration().getTimeUnitCode() == duration) {
                channelIndex++;
                if (channelIndex == index) {
                    return this.mbusDevice.getFullShadow().getRtuShadow().getChannelFullProtocolShadow().get(i).getLoadProfileIndex() - 1;
                }
            }
        }
        return -1;
    }

    private ProfileData sortOutProfiledate(ProfileData profileData, int timeDuration) {
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(profileData.getChannelInfos());
        Iterator<IntervalData> it = profileData.getIntervalIterator();
        while (it.hasNext()) {
            IntervalData id = it.next();
            switch (timeDuration) {
                case TimeDuration.DAYS: {
                    if (checkDailyBillingTime(id.getEndTime())) {
                        pd.addInterval(id);
                    }
                }
                break;
                case TimeDuration.MONTHS: {
                    if (checkMonthlyBillingTime(id.getEndTime())) {
                        pd.addInterval(id);
                    }
                }
                break;
            }
        }
        return pd;
    }

    /**
     * Checks if the given date is a date at midnight
     *
     * @param date
     * @return true or false
     */
    private boolean checkDailyBillingTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0 && cal.get(Calendar.MILLISECOND) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the given data is a date at midnight on the first of the month
     *
     * @param date
     * @return true or false
     */
    private boolean checkMonthlyBillingTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (checkDailyBillingTime(date) && cal.get(Calendar.DAY_OF_MONTH) == 1) {
            return true;
        }
        return false;
    }

    private boolean isMbusRegisterObisCode(ObisCode oc) {
//		if((oc.getC() == 24) && (oc.getD() == 2) && (oc.getB() >=1) && (oc.getB() <= 4) && (oc.getE() >= 1) && (oc.getE() <= 4) ){
        if ((oc.getC() == 24) && (oc.getD() == 2) && (oc.getB() == (mbusDevice.getPhysicalAddress() + 1)) && (oc.getE() >= 1) && (oc.getE() <= 4)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the used {@link java.util.logging.Logger}
     */
    @Override
    protected Logger getLogger() {
        return mbusDevice.getLogger();
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return this.mbusDevice.getWebRTU().getCosemObjectFactory();
    }

    private Calendar getToCalendar() {
        return this.mbusDevice.getWebRTU().getToCalendar();
    }

    private Calendar getFromCalendar(ChannelFullProtocolShadow channelFPS) {
        return this.mbusDevice.getWebRTU().getFromCalendar(channelFPS.getLastReading(), mbusDevice.getWebRTU().getTimeZone());
    }

    private DLMSMeterConfig getMeterConfig() {
        return this.mbusDevice.getWebRTU().getMeterConfig();
    }

    private TimeZone getTimeZone() {
        return this.mbusDevice.getWebRTU().getTimeZone();
    }

    /**
     * Get the dailyValues by reading the complete intervalProfile(hourly)
     *
     * @param dailyObisCode - the hourly-interval obisCode
     * @throws IOException
     */
    public ProfileData getDailyProfile(ObisCode dailyObisCode) throws IOException {
        ProfileData profileData = new ProfileData();
        ProfileGeneric genericProfile;

        genericProfile = getCosemObjectFactory().getProfileGeneric(dailyObisCode);
        List<ChannelInfo> channelInfos = getDailyMonthlyChannelInfos(genericProfile, TimeDuration.DAYS);

        if (channelInfos.size() != 0) {

            profileData.setChannelInfos(channelInfos);
            Calendar fromCalendar = null;
            Calendar channelCalendar = null;
            Calendar toCalendar = getToCalendar();

            if (this.mbusDevice.getWebRTU().isRequestOneDay()) {
                fromCalendar = ProtocolUtils.getCalendar(this.mbusDevice.getWebRTU().getTimeZone());
                fromCalendar.add(Calendar.HOUR, -24);
                this.mbusDevice.getLogger().log(Level.INFO, "Requesting One Day - from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
            } else {
                for (ChannelFullProtocolShadow channelFPS : this.mbusDevice.getFullShadow().getRtuShadow().getChannelFullProtocolShadow()) {
                    if (channelFPS.getTimeDuration().getTimeUnitCode() == TimeDuration.DAYS) {
                        channelCalendar = getFromCalendar(channelFPS);
                        if ((fromCalendar == null) || (channelCalendar.before(fromCalendar))) {
                            fromCalendar = channelCalendar;
                        }
                    }
                }
                this.mbusDevice.getLogger().log(Level.INFO, "Reading Daily values from " + fromCalendar.getTime() + " to " + toCalendar.getTime());
            }

            DataContainer dc = genericProfile.getBuffer(fromCalendar, toCalendar);
            buildProfileData(dc, profileData, genericProfile, TimeDuration.DAYS);
            ParseUtils.validateProfileData(profileData, toCalendar.getTime());
            ProfileData pd = sortOutProfiledate(profileData, TimeDuration.DAYS);
            pd.sort();
			if(mbusDevice.getWebRTU().isBadTime()){
				pd.markIntervalsAsBadTime();
			}

            return pd;
        }
        return null;
    }
}
