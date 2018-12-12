/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author Koen
 */
public class RealTimeBasePage extends AbstractBasePage {
    
    private Calendar calendar=null;
    
    /** Creates a new instance of RealTimeBasePage */
    public RealTimeBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer(); 
        strBuff.append("RealTimeBasePage:\n");
        strBuff.append("   calendar="+getCalendar()+"\n");
        strBuff.append("   date="+getCalendar().getTime()+"\n");
        return strBuff.toString();
    }   
    
    
    protected BasePageDescriptor preparebuild() throws IOException {
        BasePageDescriptor bd = new BasePageDescriptor(0x0, 7);
        if (calendar != null) {
            byte[] data = new byte[7];
            data[0] = (byte)getCalendar().get(Calendar.DAY_OF_WEEK);
            data[1] = (byte)(getCalendar().get(Calendar.YEAR)-2000);
            data[2] = (byte)(getCalendar().get(Calendar.MONTH)+1);
            data[3] = (byte)getCalendar().get(Calendar.DAY_OF_MONTH);
            data[4] = (byte)getCalendar().get(Calendar.HOUR_OF_DAY);
            data[5] = (byte)getCalendar().get(Calendar.MINUTE);
            data[6] = (byte)getCalendar().get(Calendar.SECOND);
            bd.setData(data);
        } // if (calendar != null)
        return bd;
    }
    
    protected void parse(byte[] data) throws IOException {
        TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();
        
        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);
        
        setCalendar(ProtocolUtils.getCleanCalendar(tz));
        getCalendar().set(Calendar.DAY_OF_WEEK, ProtocolUtils.getInt(data,0,1));
        int year = ProtocolUtils.getInt(data,1, 1);
        getCalendar().set(Calendar.YEAR,year>50?year+1900:year+2000);
        getCalendar().set(Calendar.MONTH, ProtocolUtils.getInt(data,2, 1) -1);
        getCalendar().set(Calendar.DAY_OF_MONTH, ProtocolUtils.getInt(data,3, 1));
        getCalendar().set(Calendar.HOUR_OF_DAY, ProtocolUtils.getInt(data,4, 1));
        getCalendar().set(Calendar.MINUTE, ProtocolUtils.getInt(data,5, 1));
        getCalendar().set(Calendar.SECOND, ProtocolUtils.getInt(data,6, 1));
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }
        
} // public class RealTimeBasePage extends AbstractBasePage
