/*
 * QueryTimeCommand.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class QueryTimeCommand extends AbstractCommand {

    private Date time;

    /** Creates a new instance of ForceStatusCommand */
    public QueryTimeCommand(CommandFactory cm) {
        super(cm);
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        Calendar systemTime = ProtocolUtils.getCalendar(getCommandFactory().getS200().getTimeZone());
        Calendar meterTime = ProtocolUtils.getCleanCalendar(getCommandFactory().getS200().getTimeZone());
        meterTime.set(Calendar.MONTH,ProtocolUtils.BCD2hex(data[offset++])-1);
        meterTime.set(Calendar.DAY_OF_MONTH,ProtocolUtils.BCD2hex(data[offset++]));
        meterTime.set(Calendar.DAY_OF_WEEK,ProtocolUtils.BCD2hex(data[offset++]));
        meterTime.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[offset++]));
        meterTime.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[offset++]));
        meterTime.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[offset++]));
        ParseUtils.adjustYear(systemTime, meterTime);
        setTime(meterTime.getTime());

    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('Q');
    }

//    protected byte[] prepareData() throws IOException {
//        return null;
//    }

    public Date getTime() {
        return time;
    }

    private void setTime(Date time) {
        this.time = time;
    }

}
