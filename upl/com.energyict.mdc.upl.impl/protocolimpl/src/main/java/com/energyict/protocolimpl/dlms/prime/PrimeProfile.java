package com.energyict.protocolimpl.dlms.prime;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.dlms.common.DLMSProfileHelper;
import com.energyict.protocolimpl.dlms.common.ProfileCacheImpl;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 22/02/12
 * Time: 13:39
 */
public class PrimeProfile extends DLMSProfileHelper {

    public static final ObisCode BASIC_PROFILE = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode DAILY_PROFILE = ObisCode.fromString("1.0.99.2.0.255");

    public static final ObisCode MONTHLY_CONTRACT1_PROFILE = ObisCode.fromString("0.0.98.1.1.255");
    public static final ObisCode MONTHLY_CONTRACT2_PROFILE = ObisCode.fromString("0.0.98.1.2.255");
    public static final ObisCode MONTHLY_CONTRACT3_PROFILE = ObisCode.fromString("0.0.98.1.3.255");
    public static final ObisCode DAILY_CONTRACT1_PROFILE = ObisCode.fromString("0.0.98.2.1.255");
    public static final ObisCode DAILY_CONTRACT2_PROFILE = ObisCode.fromString("0.0.98.2.2.255");
    public static final ObisCode DAILY_CONTRACT3_PROFILE = ObisCode.fromString("0.0.98.2.3.255");

    private static final Unit WH = Unit.get(BaseUnit.WATTHOUR);
    private static final Unit WATT = Unit.get(BaseUnit.WATT);
    private static final Unit VARH = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);
    private static final Unit MS = Unit.get(BaseUnit.SECOND, -3);

    public PrimeProfile(DlmsSession session, ObisCode profileObisCode, ProfileCacheImpl cache) {
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
        int numberOfProfileChannels = getProfileGeneric().getNumberOfProfileChannels();
        if (numberOfProfileChannels == 0) {     //Looks like ZIV meter doesn't use captured_objects...
            if (isMonthlyContractProfile()) {
                numberOfProfileChannels = 99;       //Elster monthly contract default
            } else if (isDailyContractProfile()) {
                numberOfProfileChannels = 42;       //Elster daily contract default
            } else {
                numberOfProfileChannels = 6;        //Elster LP Default
            }
        }
        return numberOfProfileChannels;
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
            getLogger().warning("Load profile captured_objects is empty, using the defaults");
            createDefaultChannelInfos();
        } else {
            int channelIndex = 0;
            for (CapturedObject capturedObject : universalObjects) {
                if (isRealChannelData(capturedObject)) {
                    Unit unit = getUnit(capturedObject);

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
                    if (ParseUtils.isObisCodeCumulative(capturedObject.getObisCode())) {
                        channelInfo.setCumulative();
                    }
                    addChannelInfo(channelInfo);
                }
            }
        }
    }

    @Override
    protected boolean isRealChannelData(CapturedObject capturedObject) {
        //This captured_object is of type DATA but is a channel!
        if (capturedObject.getClassId() == DLMSClassId.DATA.getClassId()) {
            ObisCode obisCode = capturedObject.getObisCode();
            if (obisCode.getA() == 0 && obisCode.getB() == 0 && obisCode.getC() == 0 && obisCode.getD() == 1) {    //Timestamp register, add as channel
                return true;
            }
        }
        return super.isRealChannelData(capturedObject);
    }

    protected void setClockAndStatusPosition() {
        if (isMonthlyContractProfile()) {
            setStatusMask(0);    //No status included
            setClockMask(2);     //Clock timestamp is the second interval
        } else if (isDailyContractProfile()) {
            setClockMask(1);    //Clock timestamp is the first interval
            setStatusMask(0);   //No status included
        }
    }

    /**
     * Fill in the default channelInfos, if the captured_objects attributes are empty. This is the case for all ZIV meters...
     *
     * @return default channelInfos
     */
    private void createDefaultChannelInfos() {
        int channelIndex = 0;
        if (isMonthlyContractProfile()) {
            addChannelInfo(new ChannelInfo(channelIndex++, "0.0.0.1.1" + getObisCode().getE() + ".255", MS));
        }
        if (isMonthlyContractProfile() || isDailyContractProfile()) {
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.8." + (0 + getEField()) + ".255", WH));  //E-field: 10, 20 or 30, depending on the contract (1, 2 or 3)
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.8." + (1 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.8." + (2 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.8." + (3 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.8." + (4 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.8." + (5 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.8." + (6 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.8." + (0 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.8." + (1 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.8." + (2 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.8." + (3 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.8." + (4 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.8." + (5 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.8." + (6 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.8." + (0 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.8." + (1 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.8." + (2 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.8." + (3 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.8." + (4 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.8." + (5 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.8." + (6 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.8." + (0 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.8." + (1 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.8." + (2 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.8." + (3 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.8." + (4 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.8." + (5 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.8." + (6 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.8." + (0 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.8." + (1 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.8." + (2 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.8." + (3 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.8." + (4 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.8." + (5 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.8." + (6 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.8." + (0 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.8." + (1 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.8." + (2 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.8." + (3 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.8." + (4 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.8." + (5 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.8." + (6 + getEField()) + ".255", VARH));
        }
        if (isMonthlyContractProfile()) {
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.9." + (0 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.9." + (1 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.9." + (2 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.9." + (3 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.9." + (4 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.9." + (5 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.9." + (6 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.9." + (0 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.9." + (1 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.9." + (2 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.9." + (3 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.9." + (4 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.9." + (5 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.9." + (6 + getEField()) + ".255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.9." + (0 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.9." + (1 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.9." + (2 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.9." + (3 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.9." + (4 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.9." + (5 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.9." + (6 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.9." + (0 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.9." + (1 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.9." + (2 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.9." + (3 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.9." + (4 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.9." + (5 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.9." + (6 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.9." + (0 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.9." + (1 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.9." + (2 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.9." + (3 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.9." + (4 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.9." + (5 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.9." + (6 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.9." + (0 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.9." + (1 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.9." + (2 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.9." + (3 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.9." + (4 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.9." + (5 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.9." + (6 + getEField()) + ".255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (0 + getEField()) + ".255:value", WATT));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (0 + getEField()) + ".255:time", MS));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (1 + getEField()) + ".255:value", WATT));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (1 + getEField()) + ".255:time", MS));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (2 + getEField()) + ".255:value", WATT));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (2 + getEField()) + ".255:time", MS));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (3 + getEField()) + ".255:value", WATT));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (3 + getEField()) + ".255:time", MS));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (4 + getEField()) + ".255:value", WATT));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (4 + getEField()) + ".255:time", MS));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (5 + getEField()) + ".255:value", WATT));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (5 + getEField()) + ".255:time", MS));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (6 + getEField()) + ".255:value", WATT));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.6." + (6 + getEField()) + ".255:time", MS));
        } else {
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.1.29.0.255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.2.29.0.255", WH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.5.29.0.255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.6.29.0.255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.7.29.0.255", VARH));
            addChannelInfo(new ChannelInfo(channelIndex++, "1.0.8.29.0.255", VARH));
        }
    }

    private int getEField() {
        return (10 * getObisCode().getE());
    }

    private boolean isMonthlyContractProfile() {
        return getObisCode().equals(MONTHLY_CONTRACT1_PROFILE) || getObisCode().equals(MONTHLY_CONTRACT2_PROFILE) || getObisCode().equals(MONTHLY_CONTRACT3_PROFILE);
    }

    private boolean isDailyContractProfile() {
        return getObisCode().equals(DAILY_CONTRACT1_PROFILE) || getObisCode().equals(DAILY_CONTRACT2_PROFILE) || getObisCode().equals(DAILY_CONTRACT3_PROFILE);
    }
}