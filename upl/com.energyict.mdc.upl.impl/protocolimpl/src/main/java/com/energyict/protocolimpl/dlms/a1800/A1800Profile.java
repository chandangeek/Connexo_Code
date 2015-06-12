package com.energyict.protocolimpl.dlms.a1800;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.DlmsUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.common.DLMSProfileHelper;
import com.energyict.protocolimpl.dlms.common.ProfileCacheImpl;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * read out profiles of A1800
 * <p/>
 * Created by heuckeg on 14.06.2014.
 */
@SuppressWarnings("unused")
public class A1800Profile extends DLMSProfileHelper {

    public static final ObisCode LOAD_PROFILE_PULSES = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode LOAD_PROFILE_EU_CUMULATIVE = ObisCode.fromString("1.0.99.1.1.255");
    public static final ObisCode LOAD_PROFILE_EU_NONCUMULATIVE = ObisCode.fromString("1.0.99.1.2.255");
    public static final ObisCode PROFILE_INSTRUMENTATION_SET1 = ObisCode.fromString("1.0.99.128.0.255");
    public static final ObisCode PROFILE_INSTRUMENTATION_SET2 = ObisCode.fromString("1.0.99.129.0.255");

    private static final ObisCode MULTIPLIER = ObisCode.fromString("1.1.96.132.1.255");
    private static final ObisCode SCALE_FACTOR = ObisCode.fromString("1.1.96.132.2.255");

    private Long multiplier = null;
    private int scaleFactor = 0;

    public A1800Profile(DlmsSession session, ObisCode profileObisCode, ProfileCacheImpl cache) {
        super.setSession(session);
        super.setCache(cache);
        super.setObisCode(profileObisCode);
    }

    /**
     * Read the number of channels from the device for the current profile
     *
     * @return The number of channels that contain data
     * @throws java.io.IOException If there occurred an error while reading the number of channels
     */
    public int getNumberOfChannels() throws IOException {
        final int npc = getProfileGeneric().getNumberOfProfileChannels();
        getLogger().info(String.format("Profile has %d captured objects", npc));
        return npc;
    }

    /**
     * Overriding the parent method so we can add hardcoded defaults if the captured_objects are empty (ZIV meter)
     *
     * @throws java.io.IOException
     */
    protected void readChannelInfosFromDevice() throws IOException {
        getLogger().info("Reading captured object from device for profile [" + getObisCode() + "].");

        setChannelInfos(new ArrayList<ChannelInfo>(getNumberOfChannels()));
        List<CapturedObject> universalObjects = getProfileGeneric().getCaptureObjects();
        if (universalObjects.size() == 0) {
            throw new IOException("Load profile captured_objects is empty");
        }

        readMultiplierAndScaleFactor();
        boolean hasCumulativeValues = isCumulativeProfile();

        int channelIndex = 0;
        for (CapturedObject capturedObject : universalObjects) {
            if (isRealChannelData(capturedObject)) {
                Unit u = getUnit(capturedObject);
                Unit unit = Unit.get(DlmsUnit.fromValidDlmsCode(u.getDlmsCode()).getEisUnitCode(), scaleFactor);

                //System.out.println("Unit old:" + u.toString() + "  new Unit " + unit.toString());
                final String name;
                final ObisCode generalObisCode = ProtocolTools.setObisCodeField(capturedObject.getObisCode(), 4, (byte) 0);
                final ObisCode maxValueObisCode = ObisCode.fromString("1.0.1.6.0.255");
                if (maxValueObisCode.equals(generalObisCode)) {
                    if (capturedObject.getAttributeIndex() == 2) {
                        name = capturedObject.getObisCode().toString() + ":value";
                    } else {
                        name = capturedObject.getObisCode().toString() + ":time";
                    }
                } else {
                    name = capturedObject.getObisCode().toString();
                }

                ChannelInfo channelInfo = new ChannelInfo(channelIndex++, name, unit);
                if (hasCumulativeValues) {
                    channelInfo.setCumulative();
                }

                getLogger().info(String.format("Channel %d: %s[%d] = %d - %s [%s] %s",
                        channelIndex, capturedObject.getObisCode(), capturedObject.getAttributeIndex(),
                        channelInfo.getChannelId(), channelInfo.getName(),
                        channelInfo.getUnit(), channelInfo.isCumulative()));

                addChannelInfo(channelInfo);
            }

        }
    }

    private void readMultiplierAndScaleFactor() throws IOException {
        AbstractDataType adt;
        Data scaleFactorData = getCosemObjectFactory().getData(SCALE_FACTOR);
        adt = scaleFactorData.getValueAttr();
        scaleFactor = adt.getInteger8().intValue();
        BigDecimal sf = new BigDecimal("1");
        getLogger().info("Profile scale factor: " + sf.scaleByPowerOfTen(scaleFactor));

        if (getObisCode().equals(LOAD_PROFILE_PULSES) ||
                getObisCode().equals(PROFILE_INSTRUMENTATION_SET1) ||
                getObisCode().equals(PROFILE_INSTRUMENTATION_SET2)) {
            Data multiplierData = getCosemObjectFactory().getData(MULTIPLIER);

            adt = multiplierData.getValueAttr();
            multiplier = adt.longValue();
            getLogger().info("Profile multiplier: " + multiplier);
        }
    }

    private boolean isCumulativeProfile() {
        return getObisCode().equals(LOAD_PROFILE_PULSES) ||
                getObisCode().equals(LOAD_PROFILE_EU_CUMULATIVE);
    }

    public ProfileData getProfileData(Date fromDate, Date toDate) throws IOException {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(buildChannelInfos());

        List<IntervalData> intervalData = getIntervalData(getCalendar(fromDate), getCalendar(toDate));
        profileData.setIntervalDatas(intervalData);
        return profileData;
    }

    private List<IntervalData> getIntervalData(Calendar from, Calendar to) throws IOException {
        getLogger().info("Reading interval buffer from device for profile [" + getObisCode() + "].");

        int profileEntriesInUse = getProfileGeneric().getEntriesInUse(); // The number of profile entries currently in use
        long interval = this.getProfileInterval();
        long a1800Time = getCosemObjectFactory().getClock(A1800.CLOCK_OBIS_CODE).getDateTime().getTime() / 1000;
        long fromTime = from.getTimeInMillis() / 1000;

        long entriesToRead = ((a1800Time - fromTime) / interval) + 1;
        if (profileEntriesInUse == 0){
            return new ArrayList<IntervalData>();// In case the profile buffer is empty
        } else if (entriesToRead > profileEntriesInUse) {
            entriesToRead = profileEntriesInUse; // It makes no sense to request more entries than the amount of entries currently in use
        } else if (entriesToRead < 0) {
            entriesToRead = 1;                   // This is the case when fromTime is after a1800Time, in fact telling to read out the future -> read out only the last entry
        }

        byte[] bufferData = getProfileGeneric().getBufferData(0, (int) entriesToRead, 0, 0);
        return parseBuffer(bufferData);
    }

    @Override
    public List<IntervalData> parseBuffer(byte[] bufferData) throws IOException {
        setClockAndStatusPosition();
        A1800DLMSProfileIntervals intervals = new A1800DLMSProfileIntervals(bufferData, 0x0001, 0x0002, -1, 0x0004, null);
        try {
            intervals.setMultiplier(multiplier);
            return intervals.parseIntervals(getProfileInterval(), getTimeZone());
        } catch (ClassCastException e) {
            throw new IOException(e.getMessage());
        }
    }
}