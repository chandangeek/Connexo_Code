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
public class ModemPasswordCommand extends AbstractCommand {

    private String password;

    /** Creates a new instance of ModemPasswordCommand */
    public ModemPasswordCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ModemPasswordCommand:\n");
        strBuff.append("   password="+getPassword()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        byte[] data=null;
        data = new byte[]{(byte)0xF0,0,0,0,0,0,0,0,0};
        int val = Integer.parseInt(password, 16);
        data[1] = (byte)((val>>16)&0xff);
        data[2] = (byte)((val>>8)&0xff);
        data[3] = (byte)(val&0xff);
        setResponseData(false);
        return data;
    }

    protected void parse(byte[] data) throws IOException {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
