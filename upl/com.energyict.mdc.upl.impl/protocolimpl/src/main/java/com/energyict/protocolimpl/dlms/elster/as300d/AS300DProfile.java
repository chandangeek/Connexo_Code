package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile.ApolloProfileIntervalStatusBits;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.DLMSProfileIntervals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 22/02/12
 * Time: 13:39
 */
public class AS300DProfile {

    public static final ObisCode HOURLY_PROFILE = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode DAILY_PROFILE = ObisCode.fromString("1.0.99.2.0.255");

    private final DlmsSession session;
    private final ObisCode obisCode;

    private ProfileGeneric profileGeneric = null;
    private List<ChannelInfo> channelInfos = null;

    public AS300DProfile(DlmsSession session, ObisCode profileObisCode) {
        this.session = session;
        this.obisCode = profileObisCode;
    }

    public int getProfileInterval() throws IOException {
        return getProfileGeneric().getCapturePeriod();
    }

    public int getNumberOfChannels() throws IOException {
        return getProfileGeneric().getNumberOfProfileChannels();
    }

    public ProfileData getProfileData(Date fromDate, Date toDate) throws IOException {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(getChannelInfos());
        profileData.setIntervalDatas(getIntervalDatas(getCalendar(fromDate), getCalendar(toDate)));
        return profileData;
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return session.getCosemObjectFactory();
    }

    private ProfileGeneric getProfileGeneric() throws IOException {
        if (profileGeneric == null) {
            this.profileGeneric = getCosemObjectFactory().getProfileGeneric(obisCode);
        }
        return profileGeneric;
    }

    private List<ChannelInfo> getChannelInfos() throws IOException {
        if (channelInfos == null) {
            channelInfos = new ArrayList<ChannelInfo>(getNumberOfChannels());
            List<CapturedObject> universalObjects = getProfileGeneric().getCaptureObjects();
            for (int index = 0; index < universalObjects.size(); index++) {
                CapturedObject capturedObject = universalObjects.get(index);
                if (isRealChannelData(capturedObject)) {
                    ObisCode obis = capturedObject.getObisCode();
                    Unit unit = getUnit(capturedObject);
                    String name = obis.toString();
                    ChannelInfo channelInfo = new ChannelInfo(index, name, unit);
                    if (ParseUtils.isObisCodeCumulative(obis)) {
                        channelInfo.setCumulative();
                    }
                    channelInfos.add(channelInfo);
                }
            }
        }
        return channelInfos;
    }

    private List<IntervalData> getIntervalDatas(Calendar from, Calendar to) throws IOException {
        byte[] bufferData = getProfileGeneric().getBufferData(from, to);
        DLMSProfileIntervals intervals = new DLMSProfileIntervals(bufferData, new ApolloProfileIntervalStatusBits());
        return intervals.parseIntervals(getProfileInterval());
    }

    private Calendar getCalendar(Date date) {
        Calendar calendar = Calendar.getInstance(session.getTimeZone());
        calendar.setTime(date);
        return calendar;
    }

    private boolean isRealChannelData(CapturedObject capturedObject) {
        DLMSClassId classId = DLMSClassId.findById(capturedObject.getClassId());
        return classId != DLMSClassId.CLOCK && classId != DLMSClassId.DATA;
    }

    private Unit getUnit(CapturedObject capturedObject) throws IOException {
        ObisCode obis = capturedObject.getObisCode();
        DLMSClassId classId = DLMSClassId.findById(capturedObject.getClassId());
        switch (classId) {
            case REGISTER:
                return session.getCosemObjectFactory().getRegister(obis).getScalerUnit().getEisUnit();
            case EXTENDED_REGISTER:
                return session.getCosemObjectFactory().getExtendedRegister(obis).getScalerUnit().getEisUnit();
            case DEMAND_REGISTER:
                return session.getCosemObjectFactory().getDemandRegister(obis).getScalerUnit().getEisUnit();
            default:
                return Unit.getUndefined();
        }
    }

}
