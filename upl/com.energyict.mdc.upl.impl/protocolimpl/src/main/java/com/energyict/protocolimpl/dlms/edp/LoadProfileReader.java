package com.energyict.protocolimpl.dlms.edp;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 10/02/14
 * Time: 16:42
 * Author: khe
 */
public class LoadProfileReader {

    public static final ObisCode AMR_PROFILE_STATUS_OBISCODE = ObisCode.fromString("0.0.96.10.7.255");
    private final CX20009 protocol;

    public LoadProfileReader(CX20009 protocol) {
        this.protocol = protocol;
    }

    public ProfileData readProfileData(Date from, Date to) throws IOException {
        Calendar fromCal = Calendar.getInstance(protocol.getTimeZone());
        fromCal.setTime(from);
        Calendar toCal = Calendar.getInstance(protocol.getTimeZone());
        toCal.setTime(to);
        ProfileData profileData = new ProfileData();

        ProfileGeneric profileGeneric = protocol.getCosemObjectFactory().getProfileGeneric(CX20009.PROFILE_OBISCODE);
        profileData.setChannelInfos(getChannelInfo(profileGeneric.getCaptureObjects()));
        DataContainer buffer = profileGeneric.getBuffer(fromCal, toCal);
        List<IntervalData> intervalDatas = new ArrayList<IntervalData>();
        IntervalValue value;

        for (int index = 0; index < buffer.getRoot().getElements().length; index++) {
            DataStructure structure = buffer.getRoot().getStructure(index);
            Date timeStamp = structure.getOctetString(0).toDate(protocol.getTimeZone());
            int status = structure.getInteger(1);
            List<IntervalValue> values = new ArrayList<IntervalValue>();
            for (int channel = 0; channel < profileData.getChannelInfos().size(); channel++) {
                value = new IntervalValue(structure.getInteger(channel + 2), status, getEiServerStatus(status));
                values.add(value);
            }
            intervalDatas.add(new IntervalData(timeStamp, 0, 0, 0, values));
        }
        profileData.setIntervalDatas(intervalDatas);
        return profileData;
    }

    private int getEiServerStatus(int protocolStatus) {
        int status = IntervalStateBits.OK;
        if ((protocolStatus & 0x80) == 0x80) {
            status = status | IntervalStateBits.CORRUPTED;
        }
        if ((protocolStatus & 0x40) == 0x40) {
            status = status | IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & 0x20) == 0x20) {
            status = status | IntervalStateBits.OVERFLOW;
        }
        if ((protocolStatus & 0x10) == 0x10) {
            status = status | IntervalStateBits.SHORTLONG;
        }
        if ((protocolStatus & 0x08) == 0x08) {
            status = status | IntervalStateBits.CONFIGURATIONCHANGE;
        }
        if ((protocolStatus & 0x04) == 0x04) {
            status = status | IntervalStateBits.OTHER; //LP reset
        }
        if ((protocolStatus & 0x02) == 0x02) {
            status = status | IntervalStateBits.POWERDOWN;
        }
        if ((protocolStatus & 0x01) == 0x01) {
            status = status | IntervalStateBits.POWERUP;
        }
        return status;
    }

    protected List<ChannelInfo> getChannelInfo(List<CapturedObject> capturedObjects) throws IOException {
        List<ChannelInfo> infos = new ArrayList<ChannelInfo>();
        int counter = 0;

        for (CapturedObject capturedObject : capturedObjects) {
            if (isChannel(capturedObject)) {
                ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
                Unit unit;
                try {
                    unit = protocol.readRegister(obisCode).getQuantity().getUnit();
                } catch (NoSuchRegisterException e) {
                    unit = Unit.get("");
                }
                ChannelInfo channelInfo = new ChannelInfo(counter, obisCode.toString(), unit);
                if (ParseUtils.isObisCodeCumulative(capturedObject.getLogicalName().getObisCode())) {
                    channelInfo.setCumulative();
                }
                infos.add(channelInfo);

                protocol.getLogger().info("Channel " + counter + ": " + obisCode.toString() + ", unit: " + unit.toString() + ", cumulative: " + (channelInfo.isCumulative() ? "yes" : "no"));
                counter++;
            }
        }
        return infos;
    }

    protected boolean isChannel(CapturedObject capturedObject) {
        ObisCode obisCode = capturedObject.getLogicalName().getObisCode();
        return !obisCode.equals(Clock.getDefaultObisCode()) && !obisCode.equals(AMR_PROFILE_STATUS_OBISCODE);
    }

}