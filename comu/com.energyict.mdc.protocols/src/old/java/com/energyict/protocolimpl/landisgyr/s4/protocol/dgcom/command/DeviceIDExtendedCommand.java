/*
 * TemplateCommand.java
 *
 * Created on 22 mei 2006, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DeviceIDExtendedCommand extends AbstractCommand {

    private String deviceID;

    /** Creates a new instance of TemplateCommand */
    public DeviceIDExtendedCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DeviceIDExtendedCommand:\n");
        strBuff.append("   deviceID="+getDeviceID()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        return new byte[]{(byte)0x6F,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {

        byte[] temp = new byte[data.length];
        for (int i=0;i<data.length;i++) {
            temp[(data.length-1)-i] = data[i];
        }
        setDeviceID(new String(temp));

    }

    public String getDeviceID() {
        return deviceID;
    }

    private void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
