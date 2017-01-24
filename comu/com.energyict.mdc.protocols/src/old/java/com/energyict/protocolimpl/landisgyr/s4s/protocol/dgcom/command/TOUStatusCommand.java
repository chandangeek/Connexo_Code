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

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class TOUStatusCommand extends AbstractCommand {

    private int status1;
    private int status2;
    private int status3;

    /** Creates a new instance of TemplateCommand */
    public TOUStatusCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TOUStatusCommand:\n");
        strBuff.append("   status1="+getStatus1()+"\n");
        strBuff.append("   status2="+getStatus2()+"\n");
        strBuff.append("   status3="+getStatus3()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
        return new byte[]{(byte)0x81,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setStatus1((int)data[0]&0xFF);
        setStatus2((int)data[1]&0xFF);
        if ((getCommandFactory().getFirmwareVersionCommand().isRX()) && (getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()<3.00))
            setStatus3((int)data[2]&0xFF);
    }

    public int getStatus1() {
        return status1;
    }

    public void setStatus1(int status1) {
        this.status1 = status1;
    }

    public int getStatus2() {
        return status2;
    }

    public void setStatus2(int status2) {
        this.status2 = status2;
    }

    public int getStatus3() {
        return status3;
    }

    public void setStatus3(int status3) {
        this.status3 = status3;
    }
}
