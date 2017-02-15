/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * U1600Profile.java
 *
 * Created on 26. August 2004, 16:04
 */

package com.energyict.protocolimpl.gmc.u1600;


import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.IntervalData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
//import com.energyict.protocolimpl.myprotocol.*;
/**
 *
 * @author  weinert
 */
public class U1600Profile {

    private static final int DEBUG=0;

    U1600 u1600;
    Date previousDate=null;


    /** Flags for Sommer-/Winterztimeswitching */
    boolean [] bSecondWZ = new boolean [] {false,false,false,false,false};
    /** Creates a new instance of U1600Profile */
    /** Creates a new instance of IndigoProfile */
    public U1600Profile(U1600 u1600) {
        this.u1600=u1600;

    }




    public ProfileData getProfileData(Date from, Date to) throws IOException,ProtocolConnectionException {

        previousDate=null;

        //String answerTel = "";

        int profileInterval = u1600.getProfileInterval();
        int iCountChannels = u1600.getNumberOfChannels();
        ProtocolChannelMap liCountChannels = u1600.getProtocolChannelMap();
        //String strCountChannels = liCountChannels.getChannelRegisterMap();
        iCountChannels  = liCountChannels.getNrOfProtocolChannels();
        //    iCountChannels = Integer.parseInt(strCountChannels) ;

        if (DEBUG>=1) System.out.println("KV_DEBUG> getProfileData("+from.toString()+","+to.toString()+")");
        if (to.getTime() < from.getTime()) throw new IOException("U1600Profile, getProfileData, error ("+from.toString()+") > ("+to.toString()+")");
        long offset = to.getTime() - from.getTime();
        final long ONEDAY=24*60*60*1000;
        long tostd = to.getTime() + (long)u1600.getTimeZone().getOffset(to.getTime());
        long fromstd = from.getTime() + (long)u1600.getTimeZone().getOffset(from.getTime());
        long nrOfDaysToRetrieve = ((tostd/ONEDAY) - (fromstd/ONEDAY)) + 1;



        Calendar ti_Start = ProtocolUtils.getCleanCalendar(getU1600().getTimeZone());
        ti_Start.setTime(from);
        Calendar ti_Stop = ProtocolUtils.getCleanCalendar(getU1600().getTimeZone());
        ti_Stop.setTime(to);
        Calendar startTI = ti_Start;
        Calendar stopTI  = ti_Stop;

        Calendar calendar = ProtocolUtils.getCleanCalendar(getU1600().getTimeZone());
        Date oldestda = new Date();
        String telegram = u1600.getEclConnection().getOldestPeriod(oldestda);

        int iStartByte = 0;
        calendar.set((Integer.parseInt(telegram.substring(iStartByte+6,iStartByte+8))+2000),(Integer.parseInt(telegram.substring(iStartByte+3,iStartByte+5))-1)
        , Integer.parseInt(telegram.substring(iStartByte,iStartByte+2))
        , Integer.parseInt(telegram.substring(iStartByte+9,iStartByte+11)),
                Integer.parseInt(telegram.substring(iStartByte+12,iStartByte+14)),0);
        //oldestda  = calendar.getTime();
        // System.out.println(oldestda  + "  Date");

        Calendar calendar_la = ProtocolUtils.getCleanCalendar(getU1600().getTimeZone());
        Date latestda = new Date();
        calendar_la.setTime(latestda);
        telegram = u1600.getEclConnection().getLatestPeriod(calendar_la);

        iStartByte = 0;
        calendar_la.set((Integer.parseInt(telegram.substring(iStartByte+6,iStartByte+8))+2000),(Integer.parseInt(telegram.substring(iStartByte+3,iStartByte+5))-1)
        , Integer.parseInt(telegram.substring(iStartByte,iStartByte+2))
        , Integer.parseInt(telegram.substring(iStartByte+9,iStartByte+11)),
                Integer.parseInt(telegram.substring(iStartByte+12,iStartByte+14)),0);
        //   latestda  = calendar_la.getTime();
        //     System.out.println(latestda  + " latest Date" + calendar.MONTH + "  " + calendar.DAY_OF_WEEK );

        boolean noProfileData = true;
        /* calculate UCT-Time   */
        long iUCTOldestTime  = calendar.getTimeInMillis();
        long iUCTLatestTime  = calendar_la.getTimeInMillis();
        long iStart  = ti_Start.getTimeInMillis();
        long iStop  = ti_Stop.getTimeInMillis();
        try {
            /* If Profil Data is not existing in the counter  */
            if(((iStart < iUCTOldestTime) && (iStop < iUCTOldestTime)) ||
                    ((iStart > iUCTLatestTime) && (iStop > iUCTLatestTime))){
                throw new ProtocolConnectionException("U1600Profile NODATA");
                // neu: no Data existing
            } else {
                /* Start-Timestap is older then Timestamp in UNit */
                if(iStart < iUCTOldestTime) {
                    iStart = iUCTOldestTime;
                    startTI = calendar;
                }

                /* Stop-Timestap is newerolder then Timestamp in UNit */
                if(iStop > iUCTLatestTime) {
                    iStop = iUCTLatestTime;
                    stopTI = calendar_la;
                }

                /* Calculate Periods in Uint */
                long iCount = ((((iStop/1000) - (iStart/1000)) + (profileInterval)) / (profileInterval )); //+4;
                if (!u1600.getTimeZone().inDaylightTime(ti_Start.getTime()) && u1600.getTimeZone().inDaylightTime(ti_Stop.getTime())) {
                    iCount+=4; // add an extra hour for the W->S dst transition...
                }

                // Receive Prifile
                //String iResult = getPeriods(startTI, stopTI, iCount, 96);
                ProfileData profileData = getPeriods(startTI, stopTI, iCount, 96,profileInterval,iCountChannels, liCountChannels.getProtocolChannel(0).getValue());
                profileData.sort();
                return profileData;
            }
        } catch (ConnectionException e) {
            throw new ProtocolConnectionException("U1600Error, "+e.getMessage());
        }



    }



    /**
     * Bestimmte Anzahl von Perioden auswerten
     * @param TI_Start Startperiode
     * @param TI_Stop Endperiode
     */
    // 27.10.00 RW
    private ProfileData getPeriods(Calendar ti_Start, Calendar ti_Stop, long iPeriods, long iMaxPeriods, int profileInterval, int iCountChannels, int startChannel)  throws IOException , NestedIOException, ProtocolConnectionException {
        ProfileData profileData = new ProfileData();
        IntervalData savedIntervalData=null;
        IntervalData intervalData=null;
        byte[]  byBuf; // = new byte[500000]; // KV changed! 22072005 allocation of 500K for nothing?
        long iMyPeriod = iPeriods;
        //int iStartUCT = TI_Start.getUCT();
        long iGetPeriods = iPeriods;
        Calendar ti_First = null;
        Calendar ti_Now = ProtocolUtils.getCleanCalendar(getU1600().getTimeZone());
        Date ti_nowda = new Date();
        ti_Now.setTime(ti_nowda);
        Calendar ti_Last = null;
        String strDate = "";
        String telegram = "";
        //String strBuf = "";
        String strData = "";
        int retries=0;
        //TimeInfo startTI  = new TimeInfo();
        //	TimeInfo TI_Now = new TimeInfo();
        //	TimeInfo TI_First = null;
        //	TimeInfo TI_Last = null;
        int iStartByte = 0;
        int iStopByte = 0;
        int i=0;
        while(iMyPeriod > 0) {
            /* Anzahl Perioden ermitteln */
            if(iMyPeriod > iMaxPeriods)
                iGetPeriods = iMaxPeriods;
            else
                iGetPeriods = iMyPeriod;
            /* Uhrzeit-String zusammenstellen */
            strDate = ( ti_Start.get(Calendar.DAY_OF_MONTH) + "."+ (ti_Start.get(Calendar.MONTH)+1)+ "."+ ti_Start.get(Calendar.YEAR)+ " " + ti_Start.get(Calendar.HOUR_OF_DAY)+":" + ti_Start.get(Calendar.MINUTE)+ ":15");
            u1600.getEclConnection().sendRawCommandFrame(strDate, iGetPeriods, iCountChannels, startChannel);
            byBuf = u1600.getEclConnection().receiveRawDataFrame();
            /* Datum- und Zeitwerte setzen */
            telegram = u1600.getEclConnection().bufferToString(byBuf,0,byBuf.length);
            /* check Startmark */
            if((byBuf[0] != 0x0D) || (byBuf[1] != 0x0A))
                throw new ProtocolConnectionException("Wrong Profil Frame");
            /* Auswertung der einzelnen Periode */
            iStartByte = 0;
            iStopByte = 0;
            for(i=0;i<iGetPeriods;i++) {
                /* search 0x0D & 0x0A  */
                strData = "";
                if(byBuf[iStartByte] == 0x0D){
                    strData += (char) (0xFF & byBuf[iStartByte]);
                    iStartByte++;
                }
                while((byBuf[iStartByte] != 0x0D) && (iStartByte < telegram.length()) &&
                        (byBuf[iStartByte] != 0x1A) && (iStartByte < telegram.length())) {
                    strData += (char) (0xFF & byBuf[iStartByte]);
                    iStartByte++;
                }//while((byBuf
                strData += (char) (0x1A);
                ti_Now = ti_Start;


                if (strData.length() == 1) break; // KV 05122006
                // *********************************************************************************************************************

                List channelInfos = new ArrayList();
                for (int channelIndex=0;channelIndex<u1600.getNumberOfChannels();channelIndex++) {
                    // KV TO_DO following the doc about the profile, the value after calculation is the
                    // value in W, var or VA. This seems NOR to be so!
                    channelInfos.add(new ChannelInfo(channelIndex,"U1600+ channel "+channelIndex,Unit.get("")));
                }
                profileData.setChannelInfos(channelInfos);



                //if (Debug.TEST_JOB_U1600)
                //                Debug.println("$$ Speichern :" + TI_Now.toString() + ", " + TI_Stop.toString());
                intervalData = evaluateOnePeriod(ti_Now, ti_Stop, strData, 0x0083000000000000L,iCountChannels);

                // KV 05122006
                if (isOnIntervalBoundary(intervalData)) {
                    if (savedIntervalData != null) {
                        intervalData = addIntervalData(savedIntervalData,intervalData);
                        savedIntervalData = null;
                    }
                    profileData.addInterval(intervalData);
                }
                else {
                    if (savedIntervalData == null) {
                        savedIntervalData = intervalData;
                    }
                    else {
                        savedIntervalData = addIntervalData(savedIntervalData,intervalData);
                    }
                }


                if (ti_First == null) {
                    ti_First = ti_Now;
                    ti_Last = ti_Now;
                } else
                    ti_Last = ti_Now ;
            }//for

            /* Auszuwertende Perioden inkrementieren */
            if(iMyPeriod > iMaxPeriods) {
                iMyPeriod -= iMaxPeriods;
//                long iStartUCT =  ti_Start.getTimeInMillis();
//                iStartUCT += profileInterval*iGetPeriods* 1000;
//                //  + add Time on Calendar
//                ti_Start.setTimeInMillis(iStartUCT);

                ti_Start.setTime(intervalData.getEndTime());
                ti_Start.add(Calendar.SECOND, profileInterval); // add 1 interval to avoid duplicate frame...

            } else
                iMyPeriod = 0;
        }



        return profileData;
    }

    private boolean isOnIntervalBoundary(IntervalData intervalData) throws IOException {
        return ((int)(intervalData.getEndTime().getTime()/1000) % u1600.getProfileInterval()) == 0;
    }

    // KV 15122003 changed
    private IntervalData addIntervalData(IntervalData cumulatedIntervalData,IntervalData currentIntervalData) throws IOException {
        int currentCount = currentIntervalData.getValueCount();
        IntervalData intervalData = new IntervalData(currentIntervalData.getEndTime());
        int i;
        double current;
        for (i=0;i<currentCount;i++) {
            current = ((Number)currentIntervalData.get(i)).doubleValue()+((Number)cumulatedIntervalData.get(i)).doubleValue();
            intervalData.addValue(new Double(current));
        }
        return intervalData;
    }

    /**
     * Auswertung einer Periode mit anschlie ender Speicherung in die
     * Datenbank
     * @param TI_Start , Startzeitpunkt des Periodenbereiches
     * @param TI_Stop  , Stopzeitpunkt des Periodenbereiches
     * @param Telegram , ASCII-String der U1600
     * @param Flags, Zusatzinformationen f r die Datenbank
     */
    // 28.09.00 RW
    private IntervalData  evaluateOnePeriod(Calendar ti_Start, Calendar ti_Stop,String telegram, long flags, int iNumChannels)throws IOException, NestedIOException, ProtocolConnectionException{



        //if (DEBUG>=1) System.out.println("KV_DEBUG> "+ti_Start.getTime()+", "+ti_Stop.getTime()+", 0x"+Long.toHexString(flags)+", "+telegram+", "+iNumChannels);

        IntervalData intervalData=null;

        byte[]  byBuf; //   = new byte[255];
        byte[]  byStat; //  = new byte[10];
        short   sChannelCount;
        int     iPointer;
        String  strBuf;
        long    lVal;
        Double  dVal;
        double  dValue;
        int     iUCTTime;

        //String  strValue1;// = "";
        //String  strValue2;// = "";

        double [] lValue  = new double[iNumChannels];
        //short[] sStatus = new short[iNumChannels];
        Number[] numbers = new Number[iNumChannels];

        boolean isSaved = false;

        /* Stringwerte in Byteformat konvertieren */
        byBuf  = telegram.getBytes();
        String strID =   u1600.getEclConnection().getStrID();
        byStat = strID.getBytes();
        /* Telegramm-Plausibilit t,  berpr fung der Semikolons */
        sChannelCount = 0;
        for(int i=0;i < telegram.length();i++)
            if(byBuf[i] == 0x3B)
                sChannelCount++;
        if(sChannelCount != iNumChannels+1)
            throw new ProtocolConnectionException("Wrong Profil Frame");

        /* Startkennung */
        if((byBuf[0] != 0x0D) || (byBuf[1] != 0x0A))
            throw new ProtocolConnectionException("Wrong Profil Frame");
        /*  berpr fung der Stationskennung */
        if(byStat[0] != byBuf[2])
            throw new ProtocolConnectionException("Wrong Profil Frame");
// Station mit Kennungsl nge 1
        if(strID.length() == 1) {
            if(byBuf[3] != 0x3A)
                throw new ProtocolConnectionException("Device Error");
        }
// Station mit Kennungsl nge 2
        else {
            if((byBuf[3] != byStat[1]) || (byBuf[4] != 0x3A))
                throw new ProtocolConnectionException("Device Error");
        }
        /*  berpr fung Datum und Uhrzeit */
        int iStartByte = 0;

        /* Startbyte f r Datum und Zeit suchen */
        while((byBuf[iStartByte] != 0x3A) && (iStartByte < telegram.length()))
            iStartByte++;
        iStartByte++;

        /* Datumstring aus Telegramm herausfiltern */
//String strDate = telegram.substring(iStartByte,iStartByte+14);
        String strSek = telegram.substring(iStartByte+15,iStartByte+17);
        Calendar stamp = ProtocolUtils.getCleanCalendar(getU1600().getTimeZone());
        stamp.set((Integer.parseInt(telegram.substring(iStartByte+6,iStartByte+8))+2000),(Integer.parseInt(telegram.substring(iStartByte+3,iStartByte+5))-1)
                    , Integer.parseInt(telegram.substring(iStartByte,iStartByte+2))
                    , Integer.parseInt(telegram.substring(iStartByte+9,iStartByte+11)),
                    Integer.parseInt(telegram.substring(iStartByte+12,iStartByte+14)),0);






        // Corrction for summer wintertime switching
//        long iStartUCT =  stamp.getTimeInMillis();
//        stamp.setTimeInMillis(iStartUCT);


//        int switch_Day = 0;
//        if ((stamp.get(Calendar.WEEK_OF_MONTH) == 4) &&(stamp.get(Calendar.MONTH) == 9)
//        && (stamp.get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY))
//            switch_Day = 2;
//
//        if ((stamp.get(Calendar.WEEK_OF_MONTH) == 4) &&(stamp.get(Calendar.MONTH) == 2)
//        && (stamp.get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY))
//            switch_Day = 1;

        int iSek = Integer.parseInt(strSek);
//System.out.println("Telegramm :" + strDate + "   strSek : " + strSek);
        /*  find 1. Semikolon in Telegram */
        while((byBuf[iStartByte] != 0x3B) && (iStartByte < telegram.length()))
            iStartByte++;
        iStartByte++;
        /* find 2. Semikolon in Telegram */
        while((byBuf[iStartByte] != 0x3B) && (iStartByte < telegram.length()))
            iStartByte++;
        iStartByte++;

// build the intervaldata
        //int intervalsPerDay = (3600*24)/u1600.getProfileInterval();
        sChannelCount = 0;
        for(iPointer=iStartByte;iPointer<telegram.length();iPointer++) {
            if((byBuf[iPointer] == 0x3B) || (byBuf[iPointer] == 0x1A)) {
                /* Wert in String schreiben */
                strBuf = "";
                for(int j=iStartByte;j<iPointer;j++)
                    strBuf += (char) (0xFF & byBuf[j]);
                dVal = Double.valueOf(strBuf);
                dValue = dVal.doubleValue();
                if(byBuf[iPointer] == 0x3B)
                    iStartByte = iPointer +1;

                /* Werte in Datenfeld konvertieren und speichern */
                lValue[sChannelCount] = dValue;
                numbers[sChannelCount] = dVal;
                // System.out.println("evaluateOnePeriod lValue[sChannelCount] " + lValue[sChannelCount]);
                sChannelCount++;
            }//if((byBuf[iPointer]
        }//for(iPointer

        if (DEBUG>=1) System.out.println("KV_DEBUG> "+stamp.getTime());

        // KV 30102006 fix to adjust load profile
        // SSSSSWWWW
        //      |
        //      --> Transition from summer to wintertime AND previous time with current differs <= 2 hour BUT > 1 hour --> subtract 1 hour from current
        if (previousDate != null) {
            if (getU1600().getTimeZone().inDaylightTime(previousDate) && !getU1600().getTimeZone().inDaylightTime(stamp.getTime())) {
        if (DEBUG>=1) System.out.println("KV_DEBUG> change S -> W");

                long diff = (stamp.getTime().getTime() - previousDate.getTime())/1000;
                if ((diff<=7200) && (diff>3600)) {

        if (DEBUG>=1) System.out.println("KV_DEBUG> subtract 1 hour");

                    stamp.add(Calendar.HOUR_OF_DAY,-1);
                }
            }
        }
        previousDate = stamp.getTime();

        // KV new code
        intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
        intervalData.addValues(numbers);


// Summer -> WinterHOUR_OF_DAY
// Calculate new coreect Time
//        if(( switch_Day == 2)) {
//            if(!bSecondWZ[4]) {
//                if ( (stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 0)) {
//                    bSecondWZ[4] = true;
//                    iStartUCT =  stamp.getTimeInMillis();
//                    iStartUCT = iStartUCT - (3600 * 1000);
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    isSaved = true;
//                }
//            } else {
//                if ( (stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 0)) {
//                    iStartUCT =  stamp.getTimeInMillis();
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    isSaved = true;
//                }
//            }
//            if(!bSecondWZ[0]) {
//                if((stamp.get(Calendar.HOUR) == 3) && (stamp.get(Calendar.MINUTE) == 0)) {
//                    bSecondWZ[0] = true;
//                    iStartUCT =  stamp.getTimeInMillis();
//                    iStartUCT = iStartUCT - (3600 * 1000);
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    isSaved = true;
//
//                }
//            }//if(!bSecondWZ[0])
//            else {
//                if((stamp.get(Calendar.HOUR) == 3) && (stamp.get(Calendar.MINUTE) == 0)) {
//                    iStartUCT =  stamp.getTimeInMillis();
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    isSaved = true;
////Debug.println("11 " + new TimeInfo(iUCTTime-3600).toString());
//                }
//            }
//            if(!bSecondWZ[1]) {
//                if((stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 15)) {
//                    bSecondWZ[1] = true;
//                    iStartUCT =  stamp.getTimeInMillis();
//                    iStartUCT = iStartUCT - (3600 * 1000);
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    isSaved = true;
//                }
//            } else {
//                if((stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 15)) {
//
//                    iStartUCT =  stamp.getTimeInMillis();
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    //Debug.println("22 " + new TimeInfo(iUCTTime+3600).toString());
//                    isSaved = true;
//                }
//            }//if(bSecondWZ[1])
//            if(!bSecondWZ[2]) {
//                if((stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 30)) {
//                    bSecondWZ[2] = true;
//                    iStartUCT =  stamp.getTimeInMillis();
//                    iStartUCT = iStartUCT - (3600 * 1000);
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    isSaved = true;
//                }
//            } else {
//                if ((stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 30)) {
//                    iStartUCT =  stamp.getTimeInMillis();
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    //Debug.println("33 " + new TimeInfo(iUCTTime+3600).toString());
//                    isSaved = true;
//                }
//            }
//            //if(bSecondWZ[2])
//            if(!bSecondWZ[3]) {
//                if((stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 45)) {
//                    bSecondWZ[3] = true;
//                    iStartUCT =  stamp.getTimeInMillis();
//                    iStartUCT = iStartUCT - (3600 * 1000);
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    isSaved = true;
//                }
//            } else {
//                if((stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 45)) {
//                    iStartUCT =  stamp.getTimeInMillis();
//                    stamp.setTimeInMillis(iStartUCT);
//                    intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                    intervalData.addValues(numbers);
//                    isSaved = true;
//                }
//            }//if(bSecondWZ[3])
//        }//if(ti.isSwitchingDay() == 2)
//
//        // Winter -> Summer
//        if(( switch_Day == 1)) {
//            if((stamp.get(Calendar.HOUR) == 2) && (stamp.get(Calendar.MINUTE) == 0)) {
//                iStartUCT =  stamp.getTimeInMillis();
//                iStartUCT = iStartUCT + (3600 * 1000);
//                stamp.setTimeInMillis(iStartUCT);
//                intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//                intervalData.addValues(numbers);
//                isSaved = true;
//            }
//        }
//
//
//        if(!isSaved) {
//            intervalData = new IntervalData(((Calendar)stamp. clone()).getTime());
//            intervalData.addValues(numbers);
//            isSaved = false;
//        }



        return intervalData;


    }



    private U1600 getU1600() {
        return u1600;
    }





}
