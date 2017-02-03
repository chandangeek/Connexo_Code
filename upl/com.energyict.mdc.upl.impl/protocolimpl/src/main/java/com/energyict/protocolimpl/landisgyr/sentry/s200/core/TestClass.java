/*
 * TestClass.java
 *
 * Created on 27 juli 2006, 10:09
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class TestClass {
    
    /** Creates a new instance of TestClass */
    public TestClass() {
    }
    
    public void start() {
        //ProtocolUtils.
        
        Calendar systemTime = ProtocolUtils.getCalendar(TimeZone.getTimeZone("ECT"));
        systemTime.add(Calendar.MONTH,-7);
        int systemYear = systemTime.get(Calendar.YEAR);
        
        Calendar meterTime = ProtocolUtils.getCalendar(TimeZone.getTimeZone("ECT"));
        meterTime.add(Calendar.MONTH,-8);
        meterTime.add(Calendar.DAY_OF_MONTH,+29);
        
        long diff=0;
        int saveOffset=-1;
        for (int offset = -1;offset <=1;offset++) {
            meterTime.set(Calendar.YEAR,systemYear+offset);
            long meterTimeInMs = meterTime.getTime().getTime();
            long systemTimeInMs = systemTime.getTime().getTime();
            long diff2 = Math.abs(meterTimeInMs-systemTimeInMs);
            if ((offset>-1) && (diff2 < diff)) {
                 saveOffset=offset;
            }
            diff = diff2; 
        }
        meterTime.set(Calendar.YEAR,systemYear+saveOffset);
        
        System.out.println(systemTime.getTime());
        System.out.println(meterTime.getTime());
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            TestClass tc = new TestClass();
            tc.start();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
}
