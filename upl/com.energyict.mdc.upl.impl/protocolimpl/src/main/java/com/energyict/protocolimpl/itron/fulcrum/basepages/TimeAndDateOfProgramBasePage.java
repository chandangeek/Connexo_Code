/*
 * TimeAndDateOfProgramBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class TimeAndDateOfProgramBasePage extends AbstractBasePage {
    
    private Calendar calendar;
    private Date date;
    
    /** Creates a new instance of TimeAndDateOfProgramBasePage */
    public TimeAndDateOfProgramBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TimeAndDateOfProgramBasePage:\n");
        strBuff.append("   calendar="+getCalendar()+"\n");
        strBuff.append("   date="+getDate()+"\n");
        return strBuff.toString();
    } 
    
    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x3483,0x5);
    }
    
    protected void parse(byte[] data) throws IOException {
        
        TimeZone tz = ((BasePagesFactory)getBasePagesFactory()).getFulcrum().getTimeZone();
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);
        
        Calendar calendar = ProtocolUtils.getCleanCalendar(tz); 
        calendar.set(Calendar.MINUTE,data[0]);
        calendar.set(Calendar.HOUR_OF_DAY,data[1]);
        calendar.set(Calendar.DAY_OF_MONTH,data[2]);
        calendar.set(Calendar.MONTH,data[3]-1);
        int year = data[4]>50?data[4]+1900:data[4]+2000;
        calendar.set(Calendar.YEAR,year);
        
        setDate(calendar.getTime());
        
        
    } // protected void parse(byte[] data) throws IOException

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

        
} // public class RealTimeBasePage extends AbstractBasePage
