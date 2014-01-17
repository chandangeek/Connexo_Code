/*
 * ForceStatusCommand.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ForceStatusCommand extends AbstractCommand {

    private int programMalfunction;
    private int powerDown;
    private int badCRC;
    private int badPassword;
    private int topPage;
    private int ioStatus;
    private int memorySize;


    /** Creates a new instance of ForceStatusCommand */
    public ForceStatusCommand(CommandFactory cm) {
        super(cm);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ForceStatusCommand:\n");
        strBuff.append("   badCRC="+getBadCRC()+"\n");
        strBuff.append("   badPassword="+getBadPassword()+"\n");
        strBuff.append("   ioStatus=0x"+Integer.toHexString(getIoStatus())+"\n");
        strBuff.append("   powerDown="+getPowerDown()+"\n");
        strBuff.append("   programMalfunction="+getProgramMalfunction()+"\n");
        strBuff.append("   topPage=0x"+Integer.toHexString(getTopPage())+"\n");
        strBuff.append("   memorySize="+getMemorySize()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        programMalfunction = ProtocolUtils.getInt(data,offset++,1);
        powerDown = ProtocolUtils.getInt(data,offset++,1);
        badCRC = ProtocolUtils.getInt(data,offset++,1);
        badPassword = ProtocolUtils.getInt(data,offset++,1);
        topPage = ProtocolUtils.getInt(data,offset++,1);
        ioStatus = ProtocolUtils.getInt(data,offset++,1);

        int revision = getCommandFactory().getVerifyCommand().getSoftwareVersion();
        if (revision == 3) {
            setMemorySize((topPage - 0x7F) * 0x100);
        }
        else if (revision == 4) {
            setMemorySize((topPage - 0x3F) * 0x100);
        }

    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('F');
    }

    protected byte[] prepareData() throws IOException {
        byte[] data = new byte[6];
        long unitId;
        int modeOfOperation;
        try {
            unitId = Long.parseLong(getCommandFactory().getS200().getInfoTypeNodeAddress(),16);
            modeOfOperation = getCommandFactory().getS200().getModeOfOperation();
        }
        catch(NumberFormatException e) {
            throw new IOException("ForceStatusCommand, prepareData, Node address probably not numeric! Correct first!");
        }
        data[5] = (byte)(unitId & 0xFF);
        data[4] = (byte)((unitId >> 8) & 0xFF);
        data[3] = (byte)((unitId >> 16) & 0xFF);
        data[2] = (byte)(((unitId >> 24) & 0x0F) | ((modeOfOperation<<4)&0xF0));
        data[1] = 0;
        data[0] = 0;
        return data;
    }

    public int getProgramMalfunction() {
        return programMalfunction;
    }

    private void setProgramMalfunction(int programMalfunction) {
        this.programMalfunction = programMalfunction;
    }

    public int getPowerDown() {
        return powerDown;
    }

    private void setPowerDown(int powerDown) {
        this.powerDown = powerDown;
    }

    public int getBadCRC() {
        return badCRC;
    }

    private void setBadCRC(int badCRC) {
        this.badCRC = badCRC;
    }

    public int getBadPassword() {
        return badPassword;
    }

    private void setBadPassword(int badPassword) {
        this.badPassword = badPassword;
    }

    public int getTopPage() {
        return topPage;
    }

    private void setTopPage(int topPage) {
        this.topPage = topPage;
    }

    public int getIoStatus() {
        return ioStatus;
    }

    private void setIoStatus(int ioStatus) {
        this.ioStatus = ioStatus;
    }

    public int getMemorySize() {
        return memorySize;
    }

    private void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }

}
