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

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class VerifyCommand extends AbstractCommand {

    private int startAnswerHour;
    private int stopAnswerHour;
    private int softwareVersion;
    private int year;
    private int answerDay;

    /** Creates a new instance of ForceStatusCommand */
    public VerifyCommand(CommandFactory cm) {
        super(cm);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("VerifyCommand:\n");
        strBuff.append("   answerDay="+getAnswerDay()+"\n");
        strBuff.append("   softwareVersion="+getSoftwareVersion()+"\n");
        strBuff.append("   startAnswerHour="+getStartAnswerHour()+"\n");
        strBuff.append("   stopAnswerHour="+getStopAnswerHour()+"\n");
        strBuff.append("   year="+getYear()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setStartAnswerHour(ProtocolUtils.BCD2hex(data[offset++]));
        setStopAnswerHour(ProtocolUtils.BCD2hex(data[offset++]));
        setSoftwareVersion((int)ParseUtils.getBCD2Long(data, offset, 2));
        offset+=2;
        setYear(ProtocolUtils.BCD2hex(data[offset++]));
        setAnswerDay(ProtocolUtils.BCD2hex(data[offset++]));

    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('V');
    }

    public int getStartAnswerHour() {
        return startAnswerHour;
    }

    public void setStartAnswerHour(int startAnswerHour) {
        this.startAnswerHour = startAnswerHour;
    }

    public int getStopAnswerHour() {
        return stopAnswerHour;
    }

    public void setStopAnswerHour(int stopAnswerHour) {
        this.stopAnswerHour = stopAnswerHour;
    }

    public int getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(int softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getAnswerDay() {
        return answerDay;
    }

    public void setAnswerDay(int answerDay) {
        this.answerDay = answerDay;
    }


}
