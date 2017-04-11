package com.energyict.protocolimpl.edmi.common.core;

import java.util.Date;
import java.util.TimeZone;




/**
 *
 * @author koen
 */
public class RegisterTypeDate extends AbstractRegisterType {
    
    private Date date;
    
    /** Creates a new instance of RegisterTypeDate */
    public RegisterTypeDate(TimeZone timeZone, byte[] data, boolean T, boolean D) {
        if (T & D) {
			setDate(DateTimeBuilder.getDateFromDDMMYYHHMMSS(timeZone,data));
		} else if (T & !D) {
			setDate(DateTimeBuilder.getDateFromHHMMSS(timeZone,data));
		} else if (D & !T) {
			setDate(DateTimeBuilder.getDateFromDDMMYY(timeZone,data));
		}
    }

    public Date getDate() {
        return date;
    }

    public String getString() {
        return ""+getDate();
    }
    
    public void setDate(Date date) {
        this.date = date;
    }
    
    
    
}
