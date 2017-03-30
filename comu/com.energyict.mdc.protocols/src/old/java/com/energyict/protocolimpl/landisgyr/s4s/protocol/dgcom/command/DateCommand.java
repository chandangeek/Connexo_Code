/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class DateCommand extends AbstractCommand {

    private Date date;

    /** Creates a new instance of TemplateCommand */
    public DateCommand(CommandFactory commandFactory) {
        super(commandFactory);
        date=null;
    }

    protected byte[] prepareBuild() {
        if (getDate()==null)
           return new byte[]{(byte)0x02,0,0,0,0,0,0,0,0};
        else {
            // set date
            byte[] data = new byte[]{(byte)0x22,0,0,0,0,0,0,0,0};
            Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getS4s().getTimeZone());
            cal.setTime(getDate());
            data[1] = ProtocolUtils.hex2BCD(cal.get(Calendar.DAY_OF_WEEK));
            data[2] = ProtocolUtils.hex2BCD(cal.get(Calendar.YEAR)%100);
            data[3] = ProtocolUtils.hex2BCD(cal.get(Calendar.DAY_OF_MONTH));
            data[4] = ProtocolUtils.hex2BCD(cal.get(Calendar.MONTH)+1);
            setResponseData(false);
            return data;
        }

    }

    protected void parse(byte[] data) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getS4s().getTimeZone());
        // data[0] weekday
        int year = ProtocolUtils.BCD2hex(data[1]);
        cal.set(Calendar.YEAR,year>50?1900+year:2000+year);
        cal.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[2]));
        cal.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[3])-1);
        setDate(cal.getTime());
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
