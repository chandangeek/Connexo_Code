/*
 * TemplateCommand.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class BeginRecordTimeCommand extends AbstractCommand {
    
    private Date beginRecording;
    private int profileInterval; // in minutes
    
    /** Creates a new instance of ForceStatusCommand */
    public BeginRecordTimeCommand(CommandFactory cm) {
        super(cm);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("BeginRecordTimeCommand:\n");
        strBuff.append("   beginRecording="+getBeginRecording()+"\n");
        strBuff.append("   profileInterval="+getProfileInterval()+"\n");
        return strBuff.toString();
    }         
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        Calendar cal = ProtocolUtils.getCleanCalendar(getCommandFactory().getS200().getTimeZone());
        
        cal.set(Calendar.MONTH,(ProtocolUtils.BCD2hex(data[offset++]) &0xFF)-1);
        cal.set(Calendar.DAY_OF_MONTH,(ProtocolUtils.BCD2hex(data[offset++]) &0xFF));
        cal.set(Calendar.HOUR_OF_DAY,(ProtocolUtils.BCD2hex(data[offset++]) &0xFF));
        cal.set(Calendar.MINUTE,(ProtocolUtils.BCD2hex(data[offset++]) &0xFF));
        setBeginRecording(cal.getTime());
        setProfileInterval(ProtocolUtils.BCD2hex(data[offset++]) &0xFF);
        
    }
    
    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('G');
    }

    public Date getBeginRecording() {
        return beginRecording;
    }

    public void setBeginRecording(Date beginRecording) {
        this.beginRecording = beginRecording;
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }
    
}
