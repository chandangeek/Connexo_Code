/*
 * TestClass.java
 *
 * Created on 2 november 2005, 13:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.iec1107.vdew;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class TestClass {
    
    /** Creates a new instance of TestClass */
    public TestClass() {
    }
    
    private void start() {
        Calendar cal = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("ECT"));
        
//        cal.set(Calendar.YEAR,2005);
//        cal.set(Calendar.MONTH,10-1);
//        cal.set(Calendar.DAY_OF_MONTH,30);
//        cal.set(Calendar.HOUR_OF_DAY,1);
//        cal.set(Calendar.MINUTE,59);
//        cal.set(Calendar.SECOND,59);
//        System.out.println(cal.getTime());
//        
//        cal.add(Calendar.SECOND,1);
//        System.out.println(cal.getTime());
//        
//        cal.set(Calendar.YEAR,2005);
//        cal.set(Calendar.MONTH,10-1);
//        cal.set(Calendar.DAY_OF_MONTH,30);
//        cal.set(Calendar.HOUR_OF_DAY,2);
//        cal.set(Calendar.MINUTE,00);
//        cal.set(Calendar.SECOND,0);
//        System.out.println(cal.getTime());
//
//        cal.set(Calendar.YEAR,2005);
//        cal.set(Calendar.MONTH,10-1);
//        cal.set(Calendar.DAY_OF_MONTH,30);
//        cal.set(Calendar.HOUR_OF_DAY,2);
//        cal.set(Calendar.MINUTE,30);
//        cal.set(Calendar.SECOND,0);
//        System.out.println(cal.getTime());
        
//        cal.set(Calendar.YEAR,2005);
//        cal.set(Calendar.MONTH,10-1);
//        cal.set(Calendar.DAY_OF_MONTH,30);
//        cal.set(Calendar.HOUR_OF_DAY,1);
//        cal.set(Calendar.MINUTE,0);
//        cal.set(Calendar.SECOND,0);
//        
//        for (int i=0;i<20;i++) {
//            System.out.println(TimeZone.getTimeZone("ECT").inDaylightTime(cal.getTime())+", "+cal.getTime()+", "+TimeZone.getTimeZone("ECT").inDaylightTime(cal.getTime())+", "+inDSTGreyZone(cal.getTime(),TimeZone.getTimeZone("ECT")));
//            cal.add(Calendar.MINUTE,15);
//        }
        
        
        cal = ProtocolUtils.getCleanCalendar(TimeZone.getTimeZone("ECT"));
        cal.set(Calendar.YEAR,2005);
        cal.set(Calendar.MONTH,10-1);
        cal.set(Calendar.DAY_OF_MONTH,29);
        cal.set(Calendar.HOUR_OF_DAY,5);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        System.out.println(cal.getTime());
        
        System.out.println(TimeZone.getTimeZone("ECT").inDaylightTime(new Date()));
        System.out.println(TimeZone.getTimeZone("ECT").getDSTSavings()/60000);
        
        cal.add(Calendar.MILLISECOND,-1*TimeZone.getTimeZone("ECT").getDSTSavings());
        System.out.println(cal.getTime());
        
        cal.add(Calendar.MILLISECOND,-1*TimeZone.getTimeZone("ECT").getDSTSavings());
        System.out.println(cal.getTime());
        
    }
    
    
    protected boolean inDSTGreyZone(Date date, TimeZone timeZone) {
        Date testdate;
        if (timeZone.inDaylightTime(date)) {
            testdate = new Date(date.getTime()+3600000);
            return !timeZone.inDaylightTime(testdate);
        }
        else
            testdate = new Date(date.getTime()-3600000);

        return timeZone.inDaylightTime(testdate);
    }
    
    protected Date validateLastReading(Date lastReading, TimeZone timeZone) {
        boolean isDST = timeZone.inDaylightTime(lastReading);
        Date testdate;
        if (timeZone.inDaylightTime(lastReading)) {
            testdate = new Date(lastReading.getTime()+3600000);
            if (timeZone.inDaylightTime(testdate))
                return lastReading;
            else
                return new Date(lastReading.getTime()-3600000);
        }
        else
            testdate = new Date(lastReading.getTime()-3600000);

        if (timeZone.inDaylightTime(testdate)) 
            return new Date(testdate.getTime()-3600000);
        else
            return lastReading;
    }        
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        TestClass tc = new TestClass();
        tc.start();
        
        
    }
    
}
