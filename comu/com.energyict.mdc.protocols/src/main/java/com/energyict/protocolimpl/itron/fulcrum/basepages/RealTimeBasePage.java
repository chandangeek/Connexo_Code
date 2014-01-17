/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

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
        BasePageDescriptor bd = new BasePageDescriptor(0x32BC, 7);
        if (calendar != null) {
            byte[] data = new byte[7];
            data[0] = (byte)getCalendar().get(Calendar.DAY_OF_WEEK);
            data[1] = (byte)getCalendar().get(Calendar.SECOND);
            data[2] = (byte)getCalendar().get(Calendar.MINUTE);
            data[3] = (byte)getCalendar().get(Calendar.HOUR_OF_DAY);
            data[4] = (byte)getCalendar().get(Calendar.DAY_OF_MONTH);
            data[5] = (byte)(getCalendar().get(Calendar.MONTH)+1);
            data[6] = (byte)(getCalendar().get(Calendar.YEAR)-2000);
            bd.setData(data);
        } // if (calendar != null)
        return bd;
    }

    protected void parse(byte[] data) throws IOException {
        TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();

        if (!((BasePagesFactory)getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled()) {
            tz = ProtocolUtils.getWinterTimeZone(tz);
        }


        setCalendar(ProtocolUtils.getCleanCalendar(tz));
        getCalendar().set(Calendar.DAY_OF_WEEK,data[0]);
        getCalendar().set(Calendar.SECOND,data[1]);
        getCalendar().set(Calendar.MINUTE,data[2]);
        getCalendar().set(Calendar.HOUR_OF_DAY,data[3]);
        getCalendar().set(Calendar.DAY_OF_MONTH,data[4]);
        getCalendar().set(Calendar.MONTH,data[5]-1);
        int year = data[6]>50?data[6]+1900:data[6]+2000;
        getCalendar().set(Calendar.YEAR,year);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

} // public class RealTimeBasePage extends AbstractBasePage
