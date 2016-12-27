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

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.ProtocolException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.ProtocolUtils;

import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class TimeCommand extends AbstractCommand {

    private Date time;

    /** Creates a new instance of TemplateCommand */
    public TimeCommand(CommandFactory commandFactory) {
        super(commandFactory);
        time=null;
    }

    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TemplateCommand(null)));
    }

    protected byte[] prepareBuild() {
        if (getTime()==null) {
            return new byte[]{(byte) 0x01, 0, 0, 0, 0, 0, 0, 0, 0};
        } else {
            // set time
            byte[] data = new byte[]{(byte)0x21,0,0,0,0,0,0,0,0};
            Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getS4s().getTimeZone());
            cal.setTime(getTime());
            data[1] = ProtocolUtils.hex2BCD(cal.get(Calendar.SECOND));
            data[2] = ProtocolUtils.hex2BCD(cal.get(Calendar.MINUTE));
            data[3] = ProtocolUtils.hex2BCD(cal.get(Calendar.HOUR_OF_DAY));
            setResponseData(false);
            return data;
        }

    }

    protected void parse(byte[] data) throws ProtocolException, ConnectionException, NestedIOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getS4s().getTimeZone());
        cal.set(Calendar.SECOND,ProtocolUtils.BCD2hex(data[0]));
        cal.set(Calendar.MINUTE,ProtocolUtils.BCD2hex(data[1]));
        cal.set(Calendar.HOUR_OF_DAY,ProtocolUtils.BCD2hex(data[2]));
        setTime(cal.getTime());
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
