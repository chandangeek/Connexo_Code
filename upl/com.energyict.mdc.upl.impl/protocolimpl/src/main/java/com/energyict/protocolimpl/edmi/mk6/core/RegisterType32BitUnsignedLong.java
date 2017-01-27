/*
 * RegisterType16BitsInt.java
 *
 * Created on 22 maart 2006, 9:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.core;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author koen
 */
public class RegisterType32BitUnsignedLong extends AbstractRegisterType {

    private long value;
    private Date date;
    private TimeZone timeZone;

    /** Creates a new instance of RegisterType16BitsInt */
    public RegisterType32BitUnsignedLong(byte[] data) throws ProtocolException {
        this(data, null);
    }
    public RegisterType32BitUnsignedLong(byte[] data, TimeZone timeZone) throws ProtocolException {
       setValue(ProtocolUtils.getLong(data,0,4));
       this.timeZone=timeZone;
       if (timeZone != null) {
           Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
           cal.set(Calendar.YEAR,1996);
           cal.set(Calendar.MONTH,0);
           cal.set(Calendar.DAY_OF_MONTH,1);
           cal.add(Calendar.SECOND,(int)getValue());
           setDate(cal.getTime());
       }
    }

    public BigDecimal getBigDecimal() {
        return new BigDecimal(""+value);
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public String getString() {
        if (timeZone != null) {
			return ""+getDate();
		} else {
			return ""+getValue();
		}
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }


}
