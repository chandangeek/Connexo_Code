/*
 * DownloadCommand.java
 *
 * Created on 8 september 2006, 9:27
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.protocol.schlumberger;

import com.energyict.protocolimpl.base.ParseUtils;
import com.energyict.protocolimpl.itron.protocol.SchlumbergerProtocol;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DownloadCommand extends AbstractCommand{

    private int firstAddress;
    private int lastAddress;
    private byte[] data;

    /** Creates a new instance of DownloadCommand */
    public DownloadCommand(SchlumbergerProtocol schlumbergerProtocol) {
        super(schlumbergerProtocol);
    }

    protected Command preparebuild() throws IOException {


        if (getData()==null) {
//System.out.println("Phase 1");
            Command command = new Command('D');
            byte[] data = new byte[6];
            System.arraycopy(ParseUtils.getArray(getFirstAddress(),3), 0, data, 0, 3);
            System.arraycopy(ParseUtils.getArray(getLastAddress(),3), 0, data, 3, 3);
            command.setData(data);
            return command;
        }
        else {
//System.out.println("Phase 2");
            Command command = new Command((char)0x00);
            byte[] data = new byte[getData().length];
            System.arraycopy(getData(), 0, data, 0, getData().length);
            command.setData(data);
            return command;
        }
    }

    protected void parse(byte[] data) throws IOException {
        int offset = 0;

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
