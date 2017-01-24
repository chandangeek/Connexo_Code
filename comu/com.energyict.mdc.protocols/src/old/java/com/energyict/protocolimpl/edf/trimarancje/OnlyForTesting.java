/*
 * NewMain.java
 *
 * Created on 21 juni 2006, 14:12
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarancje;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class OnlyForTesting {

    int count;

    /** Creates a new instance of NewMain */
    public OnlyForTesting() {
    }

    int profileInterval=10; // minutes

    byte[] loadTestValues() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        File file = new File("C:/Documents and Settings/koen/My Documents/projecten/edf/trimeran.txt");
        FileInputStream fis = new FileInputStream(file);
        while(true) {
            byte[] data= new byte[2];
            int retval = fis.read(data);
            if (retval==-1) {
                fis.close();
                return baos.toByteArray();

            }
            String str = new String(new byte[]{data[0],data[1]});
            int temp = Integer.parseInt(str,16);
            baos.write(temp);
        } // while(true)
    }

    void start() {
        int count;
        try {

            byte[] data = loadTestValues();
            int offset=0;
            while(true) {
                int temp = ProtocolUtils.getIntLE(data,offset, 2); offset+=2;
                if (temp == 0xFFFF){
                	break; // NumberOfElements piontage...                if (temp == 0xFFFF) break; // NumberOfElements piontage...
                }
                //System.out.println("offset="+offset);
                //offset+=4;
                if ((temp&0x8000)==0) {
                    System.out.println("value = "+temp); // value without powerfail
                } else {
                    if ((temp&0x4000)==0) {
                        System.out.println("value = "+(temp&0x3FFF)+" (powerfail)"); // value with powerfail
                    } else {


                        System.out.println("date = "+parseDate(temp&0x3FFF,TimeZone.getTimeZone("ECT")));
                    }

                }
            } // while(true)



        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private Date parseDate(int val, TimeZone timeZone) {

        int type = (val & 0x3000)>>12;
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        int day = (val & 0x0F00)>>8;
        int hour = (val & 0x00F8)>>3;
        int interval = val & 0x0007;

        System.out.println("type="+type+", day="+day+", hour="+hour+", interval="+interval);

        return cal.getTime();

    }

    private void start2() throws IOException {
        byte[] data = new byte[]{(byte)0x19,(byte)0x06,(byte)0x06,(byte)0x10,(byte)0x19,(byte)0x57,(byte)0x05,(byte)0xb1,(byte)0x90,(byte)0x00,(byte)0x00,(byte)0xb4,(byte)0xbc,(byte)0x03,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x68,(byte)0x07,(byte)0xe5,(byte)0x01,(byte)0x5d,(byte)0x17,(byte)0xc2,(byte)0x00,(byte)0x80,(byte)0x07,(byte)0x4c,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x4a,(byte)0x05,(byte)0x31,(byte)0xf8,(byte)0x48,(byte)0x02,(byte)0x6f,(byte)0x52,(byte)0xfb,(byte)0x00,(byte)0xf0,(byte)0x09,(byte)0x8c,(byte)0x00,(byte)0x00,(byte)0x05,(byte)0x00,(byte)0x75,(byte)0x05,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x04,(byte)0x06,(byte)0x04,(byte)0x14,(byte)0x05,(byte)0x14,(byte)0x05,(byte)0x0a,(byte)0x00,(byte)0x06,(byte)0x04,(byte)0x06,(byte)0x04,(byte)0xaa,(byte)0x05,(byte)0xa0,(byte)0x0f,(byte)0x00,(byte)0x00,(byte)0x00};

        int offset=0;

        Date timestamp;
        int tarif;
        int month;

        final int PEAK=0;
        final int NORMAL=1;
        final int LOW=2;

        long[] activeEnergy = new long[3]; // kWh
        long[] reactiveEnergy = new long[3]; // kvarh
        int[] nrOf10inuteIntervals = new int[3];
        int[] squareExceed = new int[3]; // kW
        int[] nrOfExceeds = new int[3];
        int[] maxDemand = new int[3]; // kW

        int subscribedPowerPeak; // kW
        int subscribedPowerNormalWinter; // kW
        int subscribedPowerLowWinter; // kW
        int subscribedPowerNormalSummer; // kW
        int subscribedPowerLowSummer; // kW
        int subscribedPowerMobile; // kW
        int subscribedPowerNormalHalfSeason; // kW
        int subscribedPowerLowHalfSeason; // kW
        int subscribedPowerLowLowSeason; // kW

        int rapport; // TCxTT
        int exceededEnergy; // kWh

        Calendar cal = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("ECT"));
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset++])-1);
        int year = ProtocolUtils.BCD2hex(data[offset++]);
        cal.set(Calendar.YEAR,year>50?1900+year:2000+year);
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset++]));
        cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset++]));
        timestamp = cal.getTime();

        tarif = data[offset++];
        month = ProtocolUtils.BCD2hex(data[offset++]);
        for (int i=0;i<3;i++) {
            activeEnergy[i] = ProtocolUtils.getLongLE(data,offset,4); offset+=4;
            reactiveEnergy[i] = ProtocolUtils.getLongLE(data,offset,4); offset+=4;
            nrOf10inuteIntervals[i] = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
            squareExceed[i] = ProtocolUtils.getIntLE(data,offset,3); offset+=3;
            nrOfExceeds[i] = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
            maxDemand[i] = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        }
        subscribedPowerPeak = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        subscribedPowerNormalWinter = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        subscribedPowerLowWinter = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        subscribedPowerNormalSummer = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        subscribedPowerLowSummer = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        subscribedPowerMobile = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        subscribedPowerNormalHalfSeason = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        subscribedPowerLowHalfSeason = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        subscribedPowerLowLowSeason = ProtocolUtils.getIntLE(data,offset,2); offset+=2;

        rapport = ProtocolUtils.getIntLE(data,offset,2); offset+=2;
        exceededEnergy = ProtocolUtils.getIntLE(data,offset,3); offset+=3;


    }

    private void start3() throws IOException {

        int retrievelMonthNrOfDays = 31;
        int retrievalDay = 19;
        int quinzaineDay = 3;

        // calc delta between retrievalday and quinzaine)
        // delta must be <= 12
        // return calculated day of month
        for (retrievelMonthNrOfDays = 28;retrievelMonthNrOfDays<=31;retrievelMonthNrOfDays++) {
            for (retrievalDay = 1;retrievalDay<=31;retrievalDay++) {
                for (quinzaineDay = 0;quinzaineDay<=15;quinzaineDay++) {
                    int deltaL,deltaH;
                    int temp = retrievalDay-quinzaineDay;
                    if (temp<0) {
						deltaL = retrievelMonthNrOfDays -Math.abs(temp);
					} else {
						deltaL=temp;
					}
                    temp = retrievalDay-(quinzaineDay+16);
                    if (temp<0) {
						deltaH = retrievelMonthNrOfDays -Math.abs(temp);
					} else {
						deltaH=temp;
					}
                    //if (deltaL==deltaH)
                       System.out.println("deltaL="+deltaL+", deltaH="+deltaH+", retrievelMonthNrOfDays="+retrievelMonthNrOfDays+", retrievalDay="+retrievalDay+", quinzaineDay="+quinzaineDay);
                }
            }
        }


    }



    private Calendar getCalendarDayAndMonth(int quinzaineDay, Calendar retrievalCalendar) {


         int retrievalDayOfMonth = retrievalCalendar.get(Calendar.DAY_OF_MONTH);
         int retrievalMonth = retrievalCalendar.get(Calendar.MONTH);

//         retrievalCalendar.add(Calendar.DAY_OF_MONTH, -13);
//         int earliestRetrievalDayOfMonth = retrievalCalendar.get(Calendar.DAY_OF_MONTH);
//         int earliestRetrievalMonth = retrievalCalendar.get(Calendar.MONTH);

         int intervalDay;
         int intervalMonth;



         if (retrievalDayOfMonth < 16) {
             if (quinzaineDay > retrievalDayOfMonth) {
                 // previous month, quinzaine 2
                 intervalDay = quinzaineDay+16; // 0=16, 1=17, ... 15=31
                 intervalMonth = retrievalMonth--<=0?11:retrievalMonth;
             }
             else {
                 // current month, quinzaine 1
                 intervalDay = quinzaineDay; // 1=1, 2=2, ... 15=15
                 intervalMonth = retrievalMonth;
             }

         }
         else {
             if (quinzaineDay > (retrievalDayOfMonth-16)) {
                 // current month, quinzaine 1
                 intervalDay = quinzaineDay; // 1=1, 2=2, ... 15=15
                 intervalMonth = retrievalMonth;
             }
             else {
                 // current month, quinzaine 2
                 intervalDay = quinzaineDay+16; // 0=16, 1=17, ... 15=31
                 intervalMonth = retrievalMonth;
             }
         }

         Calendar intervalCal = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("ECT"));
         intervalCal.set(Calendar.MONTH,intervalMonth);
         intervalCal.set(Calendar.DAY_OF_MONTH,intervalDay);
         return intervalCal;
    }

     private void start4() throws IOException {

         int[] quinzaineDays= new int[]{12,11,10,9,8,7,6,5,4,3,2,1,12,11};


         Calendar retrievalCalendar = ProtocolUtils.getCalendar(TimeZone.getTimeZone("ECT"));

         retrievalCalendar.set(Calendar.YEAR,2006);
         retrievalCalendar.set(Calendar.MONTH,2);
         retrievalCalendar.set(Calendar.DAY_OF_MONTH,30);

         Calendar previousIntervalCalendar=null;

         for (int quinzaineday=0;quinzaineday<quinzaineDays.length;quinzaineday++) {
             System.out.println("now = "+retrievalCalendar.getTime());
             Calendar intervalCalendar = getCalendarDayAndMonth(quinzaineDays[quinzaineday], retrievalCalendar);
             intervalCalendar.set(Calendar.YEAR,retrievalCalendar.get(Calendar.YEAR));
             intervalCalendar.set(Calendar.HOUR_OF_DAY,retrievalCalendar.get(Calendar.HOUR_OF_DAY));

             if ((previousIntervalCalendar != null) && (intervalCalendar.getTime().after(previousIntervalCalendar.getTime()))) {
                 intervalCalendar.add(Calendar.DAY_OF_MONTH,-12);
                 System.out.println("interval calendar after correction (-12) = "+intervalCalendar.getTime());
             } else {
				System.out.println("interval calendar = "+intervalCalendar.getTime());
			}

             previousIntervalCalendar = intervalCalendar;
         }
     }

}
