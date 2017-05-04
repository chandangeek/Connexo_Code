package com.energyict.protocolimpl.dlms;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.protocolimpl.dlms.common.NTASecurityProvider;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

public class DLMSEICT extends DLMSSN {

    @Override
    public String getProtocolDescription() {
        return "EnergyICT RTU DLMS";
    }

    @Inject
    public DLMSEICT(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService, ormClient);
    }

    protected String getDeviceID() {
        return "EIT";
    }

    // Interval List
    private static final byte IL_CAPUTURETIME=0;
    private static final byte IL_EVENT=12;
    private static final byte IL_DEMANDVALUE=13;

    // Event codes as interpreted by MV90 for the Siemens ZMD meter
    private static final long EV_NORMAL_END_OF_INTERVAL=0x00800000;
    private static final long EV_START_OF_INTERVAL=     0x00080000;
    private static final long EV_FATAL_ERROR=           0x00000001;
    private static final long EV_CORRUPTED_MEASUREMENT= 0x00000004;
    private static final long EV_TIME_DATE_ADJUSTED=    0x00000020;
    private static final long EV_POWER_UP=              0x00000040;
    private static final long EV_POWER_DOWN=            0x00000080;
    private static final long EV_EVENT_LOG_CLEARED=     0x00002000;
    private static final long EV_LOAD_PROFILE_CLEARED=  0x00004000;
    private static final long EV_CAPTURED_EVENTS=       0x008860E5; // Add new events...

    protected void getEventLog(ProfileData profileDate,Calendar fromCalendar, Calendar toCalendar) throws IOException {
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

    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    protected void buildProfileData(byte bNROfChannels,ProfileData profileData,ScalerUnit[] scalerunit,UniversalObject[] intervalList)  throws IOException
    {
        byte bDOW;
        Calendar stdCalendar=null;
        Calendar dstCalendar=null;
        Calendar calendar=null;
        int i,t;

        if (isRequestTimeZone()) {
           stdCalendar = ProtocolUtils.getCalendar(false,requestTimeZone());
           dstCalendar = ProtocolUtils.getCalendar(true,requestTimeZone());
        }
        else
           calendar = ProtocolUtils.initCalendar(false,getTimeZone());

        for (i=(intervalList.length-1);i>=0;i--)
        {
            if (isRequestTimeZone()) {
            if (intervalList[i].getField(IL_CAPUTURETIME+11) != 0xff) {
                if ((intervalList[i].getField(IL_CAPUTURETIME+11)&0x80) == 0x80) calendar = dstCalendar;
                else calendar = stdCalendar;
              }
              else calendar = stdCalendar;
            }

           // Build Timestamp
           calendar.set(Calendar.YEAR,(int)((intervalList[i].getField(IL_CAPUTURETIME)<<8) |
                                            intervalList[i].getField(IL_CAPUTURETIME+1)));
           calendar.set(Calendar.MONTH,(int)intervalList[i].getField(IL_CAPUTURETIME+2)-1);
           calendar.set(Calendar.DAY_OF_MONTH,(int)intervalList[i].getField(IL_CAPUTURETIME+3));
           calendar.set(Calendar.HOUR_OF_DAY,(int)intervalList[i].getField(IL_CAPUTURETIME+5));
           calendar.set(Calendar.MINUTE,(int)intervalList[i].getField(IL_CAPUTURETIME+6));
           calendar.set(Calendar.SECOND,(int)intervalList[i].getField(IL_CAPUTURETIME+7));

           int iField = (int)intervalList[i].getField(IL_EVENT) & (int)EV_CAPTURED_EVENTS;
           iField &= (EV_NORMAL_END_OF_INTERVAL ^ 0xffffffff); // exclude EV_NORMAL_END_OF_INTERVAL bit
           for (int bit=0x1;bit!=0;bit<<=1)
           {
               if ((iField & bit) != 0)
               {
                   profileData.addEvent(new MeterEvent(new Date(((Calendar)calendar.clone()).getTime().getTime()),
                                                       (int)mapLogCodes(bit),
                                                       (int)bit));
               }
           } // for (int bit=0x1;bit!=0;bit<<=1)

           // KV 12112002 following the Siemens integration handbook, only exclude profile entries where status & EV_START_OF_INTERVAL is true
           //if ((intervalList[i].getField(IL_EVENT) & EV_NORMAL_END_OF_INTERVAL) != 0)
           if ((intervalList[i].getField(IL_EVENT) & EV_START_OF_INTERVAL) == 0)
           {
              // Fill profileData
              IntervalData intervalData = new IntervalData(new Date(((Calendar)calendar.clone()).getTime().getTime()));

              for (t=0;t<bNROfChannels;t++)
                 intervalData.addValue(new Long(intervalList[i].getField(IL_DEMANDVALUE+t)));

              if ((intervalList[i].getField(IL_EVENT) & EV_CORRUPTED_MEASUREMENT) != 0)
                  intervalData.addStatus(IntervalData.CORRUPTED);


              profileData.addInterval(intervalData);

           }

        } // for (i=0;i<intervalList.length;i++)
    } // ProfileData buildProfileData(...)

    private long mapLogCodes(long lLogCode)
    {
        switch((int)lLogCode)
        {
            case (int)EV_FATAL_ERROR: return(MeterEvent.FATAL_ERROR);
            case (int)EV_CORRUPTED_MEASUREMENT: return(MeterEvent.OTHER);
            case (int)EV_TIME_DATE_ADJUSTED: return(MeterEvent.SETCLOCK);
            case (int)EV_POWER_UP: return(MeterEvent.POWERUP);
            case (int)EV_POWER_DOWN: return(MeterEvent.POWERDOWN);
            case (int)EV_EVENT_LOG_CLEARED: return(MeterEvent.OTHER);
            case (int)EV_LOAD_PROFILE_CLEARED: return(MeterEvent.CLEAR_DATA);
            default: return(MeterEvent.OTHER);
        } // switch(lLogCode)
    } // private void mapLogCodes(long lLogCode)

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator= getRequiredKeys().iterator();
            while (iterator.hasNext())
            {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null)
                    throw new MissingPropertyException (key + " key missing");
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            if (strID.length()>16) throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            //if (strPassword.length()!=8) throw new InvalidPropertyException("Password must be exact 8 characters.");
            iHDLCTimeoutProperty=Integer.parseInt(properties.getProperty("Timeout","10000").trim());
            iProtocolRetriesProperty=Integer.parseInt(properties.getProperty("Retries","5").trim());
            iDelayAfterFailProperty=Integer.parseInt(properties.getProperty("DelayAfterfail","3000").trim());
            iRequestTimeZone=Integer.parseInt(properties.getProperty("RequestTimeZone","1").trim());
            iRequestClockObject=Integer.parseInt(properties.getProperty("RequestClockObject","0").trim());
            iRoundtripCorrection=Integer.parseInt(properties.getProperty("RoundtripCorrection","0").trim());
            iSecurityLevelProperty=Integer.parseInt(properties.getProperty("SecurityLevel","1").trim());
            iClientMacAddress=Integer.parseInt(properties.getProperty("ClientMacAddress","32").trim());
            iServerUpperMacAddress=Integer.parseInt(properties.getProperty("ServerUpperMacAddress","1").trim());
            iServerLowerMacAddress=Integer.parseInt(properties.getProperty("ServerLowerMacAddress","0").trim());
        }
        catch (NumberFormatException e) {
           throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "+e.getMessage());
        }
    }


} // public class DLMSEICT
