/*
 * UploadCommand.java
 *
 * Created on 8 september 2006, 9:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class UploadCommand extends AbstractCommand{

    private int firstAddress;
    private int lastAddress;
    private byte[] data;

    /** Creates a new instance of UploadCommand */
    public UploadCommand(SchlumbergerProtocol schlumbergerProtocol) {
        super(schlumbergerProtocol);
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("UploadCommand:\n");
        for (int i=0;i<getData().length;i++) {
            strBuff.append("       data["+i+"]=0x"+Integer.toHexString((int)getData()[i]&0xFF)+"\n");
        }
        strBuff.append("   firstAddress=0x"+Integer.toHexString(getFirstAddress())+"\n");
        strBuff.append("   lastAddress=0x"+Integer.toHexString(getLastAddress())+"\n");
        return strBuff.toString();
    }

    protected Command preparebuild() throws IOException {
        Command command = new Command('U');
        byte[] frame = new byte[6];
        System.arraycopy(ParseUtils.getArray(getFirstAddress(),3), 0, frame, 0, 3);
        System.arraycopy(ParseUtils.getArray(getLastAddress(),3), 0, frame, 3, 3);
        command.setExpectedDataLength(getLastAddress()-getFirstAddress()+1);
        command.setData(frame);
        return command;
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;
        setData(ProtocolUtils.getSubArray2(data,0, data.length-2));
    }

    public int getFirstAddress() {
        return firstAddress;
    }

    public void setFirstAddress(int firstAddress) {
        this.firstAddress = firstAddress;
    }

    public int getLastAddress() {
        return lastAddress;
    }

    public void setLastAddress(int lastAddress) {
        this.lastAddress = lastAddress;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }



}
