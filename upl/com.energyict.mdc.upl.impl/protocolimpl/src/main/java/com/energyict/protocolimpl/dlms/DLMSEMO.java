/**
 * @version 2.0
 * @author Koenraad Vanderschaeve
 * <p>
 * <B>Description :</B><BR>
 * Class that implements the Enermet E700 DLMS profile implementation
 * <BR>
 * <B>@beginchanges</B><BR>
 * KV|08042003|Initial version
 * KV|23032005|Changed header to be compatible with protocol version tool
 * KV|31032005|Handle DataContainerException
 * @endchanges
 */
package com.energyict.protocolimpl.dlms;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;

public class DLMSEMO extends DLMSSN {

    protected String getDeviceID() {
        return "EMO";
    }

    // Interval List
    private static final byte IL_CAPUTURETIME = 0;
    private static final byte IL_EVENT = 12;
    private static final byte IL_DEMANDVALUE = 13;

    private static final long EV_FATAL_ERROR = 0x00000001;
    private static final long EV_DST_ACTIVE = 0x00000008;
    private static final long EV_TIME_DATE_ADJUSTED = 0x00000020;
    private static final long EV_POWER_DOWN = 0x00000080;
    private static final long EV_CAPTURED_EVENTS = 0x000000A9; // Add new events...

    public DLMSEMO(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
    protected void getEventLog(ProfileData profileDate, Calendar fromCalendar, Calendar toCalendar) throws IOException {
    }

    @Override
    protected SecurityProvider getSecurityProvider() {
        return new NTASecurityProvider(getProperties());
    }

    /**
     * Configure the {@link com.energyict.dlms.aso.ConformanceBlock} which is used for the DLMS association.
     *
     * @return the conformanceBlock, if null is returned then depending on the reference,
     *         the default value({@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_LN_CONFORMANCE_BLOCK} or {@link com.energyict.dlms.aso.ConformanceBlock#DEFAULT_SN_CONFORMANCE_BLOCK}) will be used
     */
    @Override
    protected ConformanceBlock configureConformanceBlock() {
        return new ConformanceBlock(1573408L);
    }

    @Override
    protected void buildProfileData(byte bNROfChannels, ProfileData profileData, ScalerUnit[] scalerunit, UniversalObject[] intervalList) throws IOException {
        Calendar stdCalendar = null;
        Calendar dstCalendar = null;
        Calendar calendar = null;
        int i, t;

        if (isRequestTimeZone()) {
            stdCalendar = ProtocolUtils.getCalendar(false, requestTimeZone());
            dstCalendar = ProtocolUtils.getCalendar(true, requestTimeZone());
        } else {
            calendar = ProtocolUtils.initCalendar(false, getTimeZone());
        }

        for (i = (intervalList.length - 1); i >= 0; i--) {
            if (isRequestTimeZone()) {
                if (intervalList[i].getField(IL_CAPUTURETIME + 11) != 0xff) {
                    if ((intervalList[i].getField(IL_CAPUTURETIME + 11) & 0x80) == 0x80) {
                        calendar = dstCalendar;
                    } else {
                        calendar = stdCalendar;
                    }
                } else {
                    calendar = stdCalendar;
                }
            }

            // Build Timestamp
            calendar.set(Calendar.YEAR, (int) ((intervalList[i].getField(IL_CAPUTURETIME) << 8) |
                    intervalList[i].getField(IL_CAPUTURETIME + 1)));
            calendar.set(Calendar.MONTH, (int) intervalList[i].getField(IL_CAPUTURETIME + 2) - 1);
            calendar.set(Calendar.DAY_OF_MONTH, (int) intervalList[i].getField(IL_CAPUTURETIME + 3));
            calendar.set(Calendar.HOUR_OF_DAY, (int) intervalList[i].getField(IL_CAPUTURETIME + 5));
            calendar.set(Calendar.MINUTE, (int) intervalList[i].getField(IL_CAPUTURETIME + 6));
            calendar.set(Calendar.SECOND, (int) intervalList[i].getField(IL_CAPUTURETIME + 7));

            int iField = (int) intervalList[i].getField(IL_EVENT) & (int) EV_CAPTURED_EVENTS;
            iField &= (EV_DST_ACTIVE ^ 0xFFFFFFFF); // filter out DST flag
            for (int bit = 0x1; bit != 0; bit <<= 1) {
                if ((iField & bit) != 0) {
                    profileData.addEvent(
                            new MeterEvent(
                                    new Date(((Calendar) calendar.clone()).getTime().getTime()),
                                    (int) mapLogCodes(bit),
                                    bit));
                }
            } // for (int bit=0x1;bit!=0;bit<<=1)

            // Fill profileData
            IntervalData intervalData = new IntervalData(new Date(((Calendar) calendar.clone()).getTime().getTime()));
            for (t = 0; t < bNROfChannels; t++) {
                intervalData.addValue(new Long(intervalList[i].getField(IL_DEMANDVALUE + t)));
            }

            if (iField != 0) {
                intervalData.addStatus(IntervalData.CORRUPTED);
            }

            if ((intervalList[i].getField(IL_EVENT) & EV_FATAL_ERROR) != 0) {
                intervalData.addStatus(IntervalData.CORRUPTED);
            }
            if ((intervalList[i].getField(IL_EVENT) & EV_TIME_DATE_ADJUSTED) != 0) {
                intervalData.addStatus(IntervalData.SHORTLONG);
            }
            if ((intervalList[i].getField(IL_EVENT) & EV_POWER_DOWN) != 0) {
                intervalData.addStatus(IntervalData.POWERDOWN);
            }
            profileData.addInterval(intervalData);
        }
    }

    private long mapLogCodes(long lLogCode) {
        switch ((int) lLogCode) {
            case (int) EV_FATAL_ERROR:
                return (MeterEvent.FATAL_ERROR);
            case (int) EV_TIME_DATE_ADJUSTED:
                return (MeterEvent.SETCLOCK);
            case (int) EV_POWER_DOWN:
                return (MeterEvent.POWERDOWN);
            default:
                return (MeterEvent.OTHER);
        }
    }

    @Override
    protected void doSetProperties(TypedProperties properties) throws PropertyValidationException {
        super.doSetProperties(properties);
        this.setSecurityLevelProperty(Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "1").trim()));
        this.setClientMacAddress(properties.getTypedProperty(PROPNAME_CLIENT_MAC_ADDRESS, BigDecimal.valueOf(58)).intValue());
        this.setServerUpperMacAddress(Integer.parseInt(properties.getTypedProperty(PROPNAME_SERVER_UPPER_MAC_ADDRESS, "74").trim()));
        this.setServerLowerMacAddress(Integer.parseInt(properties.getTypedProperty(PROPNAME_SERVER_LOWER_MAC_ADDRESS, "0").trim()));
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: Thu Dec 29 09:39:05 2016 +0100 $";
    }

    @Override
    public String getProtocolDescription() {
        return "Enernet E7xx DLMS";
    }

}