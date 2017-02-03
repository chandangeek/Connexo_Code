/*
 * RealTimeBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.vectron.basepages;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Koen
 */
public class RealTimeBasePage extends AbstractBasePage {

    private Calendar calendar = null;

    /**
     * Creates a new instance of RealTimeBasePage
     */
    public RealTimeBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RealTimeBasePage:\n");
        strBuff.append("   calendar=" + getCalendar() + "\n");
        strBuff.append("   date=" + getCalendar().getTime() + "\n");
        return strBuff.toString();
    }


    protected BasePageDescriptor preparebuild() throws IOException {
        BasePageDescriptor bd = new BasePageDescriptor(0x20F9, 7);
        if (calendar != null) {
            byte[] data = new byte[7];
            data[6] = ProtocolUtils.hex2BCD(getCalendar().get(Calendar.DAY_OF_WEEK));
            data[5] = ProtocolUtils.hex2BCD(getCalendar().get(Calendar.SECOND));
            data[4] = ProtocolUtils.hex2BCD(getCalendar().get(Calendar.MINUTE));
            data[3] = ProtocolUtils.hex2BCD(getCalendar().get(Calendar.HOUR_OF_DAY));
            data[2] = ProtocolUtils.hex2BCD(getCalendar().get(Calendar.DAY_OF_MONTH));
            data[1] = ProtocolUtils.hex2BCD((getCalendar().get(Calendar.MONTH) + 1));
            data[0] = ProtocolUtils.hex2BCD((getCalendar().get(Calendar.YEAR) - 2000));
            bd.setData(data);
        } // if (calendar != null)
        return bd;
    }

    protected void parse(byte[] data) throws IOException {
        TimeZone tz = getBasePagesFactory().getProtocolLink().getTimeZone();

        if (!((BasePagesFactory) getBasePagesFactory()).getOperatingSetUpBasePage().isDstEnabled())
            tz = ProtocolUtils.getWinterTimeZone(tz);

        setCalendar(ProtocolUtils.getCleanCalendar(tz));
        getCalendar().set(Calendar.DAY_OF_WEEK, (int) ParseUtils.getBCD2Long(data, 6, 1));
        getCalendar().set(Calendar.SECOND, (int) ParseUtils.getBCD2Long(data, 5, 1));
        getCalendar().set(Calendar.MINUTE, (int) ParseUtils.getBCD2Long(data, 4, 1));
        getCalendar().set(Calendar.HOUR_OF_DAY, (int) ParseUtils.getBCD2Long(data, 3, 1));
        getCalendar().set(Calendar.DAY_OF_MONTH, (int) ParseUtils.getBCD2Long(data, 2, 1));
        getCalendar().set(Calendar.MONTH, (int) ParseUtils.getBCD2Long(data, 1, 1) - 1);
        int year = (int) ParseUtils.getBCD2Long(data, 0, 1);
        getCalendar().set(Calendar.YEAR, year > 50 ? year + 1900 : year + 2000);
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        this.calendar = calendar;
    }

} // public class RealTimeBasePage extends AbstractBasePage
