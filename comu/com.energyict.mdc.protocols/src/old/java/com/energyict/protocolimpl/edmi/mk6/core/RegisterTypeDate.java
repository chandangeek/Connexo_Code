/*
 * RegisterTypeDate.java
 *
 * Created on 23 maart 2006, 11:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.core;

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
