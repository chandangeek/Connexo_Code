package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import com.energyict.cbo.Unit;
import com.energyict.dlms.AbstractDLMSProfile;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 9-sep-2010
 * Time: 13:47:06
 */
public class LoggerProfile extends AbstractDLMSProfile {

    private static final ObisCode STATUS_OBISCODE = ObisCode.fromString("0.0.96.10.1.255");

    private final int firstChannelIndex;
    private final ObisCode profileObisCode;
    private final CosemObjectFactory cosemObjectFactory;

    private List<ChannelInfo> channelInfos = null;
    private ProfileGeneric profileGeneric = null;
    private List<CapturedObject> capturedObjects = null;

    public LoggerProfile(int firstChannelIndex, ObisCode profileObisCode, CosemObjectFactory cosemObjectFactory) {
        this.firstChannelIndex = firstChannelIndex;
        this.profileObisCode = profileObisCode;
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public ProfileData getProfileData(Calendar fromCalendar) throws IOException {
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfos());
        pd.setIntervalDatas(readIntervalDatas(fromCalendar));
        pd.sort();
        return pd;
    }

    private List<IntervalData> readIntervalDatas(Calendar fromCalendar) throws IOException {
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        byte[] rawData = getProfileGeneric().getBufferData(fromCalendar);
        Array profileArray = new Array(rawData, 0, 0);
        for (int i = 0; i < profileArray.nrOfDataTypes(); i++) {
            AbstractDataType dataType = profileArray.getDataType(i);
            if (dataType instanceof Structure) {
                Structure intervalStructure = (Structure) dataType;
                int ptr = 0;
                Date timeStamp = intervalStructure.getDataType(ptr++).getOctetString().getDateTime(null).getValue().getTime();
                int intervalStatus = intervalStructure.getDataType(ptr++).intValue();
                List<IntervalValue> intervalValues = new ArrayList<IntervalValue>();
                int eiIntervalStatus = intervalStatus;
                while (ptr < intervalStructure.nrOfDataTypes()) {
                    BigDecimal value = intervalStructure.getDataType(ptr++).toBigDecimal();
                    IntervalValue intervalValue = new IntervalValue(value, intervalStatus, eiIntervalStatus);
                    intervalValues.add(intervalValue);
                }
                IntervalData intervalData = new IntervalData(timeStamp, eiIntervalStatus, intervalStatus, 0, intervalValues);
                intervalDatas.add(intervalData);
            }
        }
        return intervalDatas;
    }

    private List<ChannelInfo> createChannelInfos() throws IOException {
        List<ChannelInfo> cis = new ArrayList<ChannelInfo>();
        try {
            for (CapturedObject capturedObject : getCapturedObjects()) {
                boolean isTimeStamp = capturedObject.getClassId() == DLMSClassId.CLOCK.getClassId();
                ObisCode capturedObisCode = capturedObject.getLogicalName().getObisCode();
                boolean isStatus = capturedObisCode.equals(STATUS_OBISCODE);
                if (!isTimeStamp && !isStatus) {
                    Unit unit = getUnit(capturedObisCode);
                    String name = capturedObisCode.toString();
                    ChannelInfo channelInfo = new ChannelInfo(cis.size(), firstChannelIndex + cis.size(), name, unit);
                    if (ParseUtils.isObisCodeCumulative(capturedObisCode)) {
                        channelInfo.setCumulative();
                    }
                    capturedObisCode.anyChannel();
                    cis.add(channelInfo);
                }
            }
        } catch (IOException e) {
            throw new IOException("Unable to read the channel info: " + e.getMessage());
        }
        return cis;
    }

    protected CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    @Override
    protected ObisCode getCorrectedObisCode(ObisCode baseObisCode) {
        return baseObisCode;
    }

    private ProfileGeneric getProfileGeneric() throws IOException {
        if (profileGeneric == null) {
            profileGeneric = getCosemObjectFactory().getProfileGeneric(profileObisCode);
        }
        return profileGeneric;
    }

    public List<ChannelInfo> getChannelInfos() throws IOException {
        if (channelInfos == null) {
            channelInfos = createChannelInfos();
        }
        return channelInfos;
    }

    private List<CapturedObject> getCapturedObjects() throws IOException {
        if (capturedObjects == null) {
            this.capturedObjects = getProfileGeneric().getCaptureObjects();
        }
        return capturedObjects;
    }

}
