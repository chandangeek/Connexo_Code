/*
 * GCCommand.java
 *
 * Created on 13 oktober 2005, 14:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class GCCommand  extends AbstractCommand {

    private static final CommandIdentification commandIdentification = new CommandIdentification("GC");

    private Date date;

    /** Creates a new instance of GCCommand */
    public GCCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        return "GCCommand: "+getDate();
    }

    protected void parse(String strData) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(strData));
        Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getMarkV().getTimeZone());
        cal.set(Calendar.SECOND,Integer.parseInt(br.readLine()));
        cal.set(Calendar.MINUTE,Integer.parseInt(br.readLine()));
        cal.set(Calendar.HOUR_OF_DAY,Integer.parseInt(br.readLine()));
        cal.set(Calendar.DAY_OF_MONTH,Integer.parseInt(br.readLine()));
        cal.set(Calendar.MONTH,Integer.parseInt(br.readLine())-1);
        int year = Integer.parseInt(br.readLine());
        cal.set(Calendar.YEAR,year>=50?year+1900:year+2000);
        setDate(cal.getTime());
    }

    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
