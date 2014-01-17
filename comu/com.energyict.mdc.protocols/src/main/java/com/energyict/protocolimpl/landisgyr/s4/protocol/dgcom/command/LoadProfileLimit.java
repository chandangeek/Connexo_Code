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

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class LoadProfileLimit extends AbstractCommand {

    private final int DEBUG=0;
    private int smallLoadProfilePartitionSize;
    private int largeLoadProfilePartitionSize;

    /** Creates a new instance of TemplateCommand */
    public LoadProfileLimit(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileLimit:\n");
        strBuff.append("   smallLoadProfilePartitionSize="+getSmallLoadProfilePartitionSize()+"\n");
        strBuff.append("   largeLoadProfilePartitionSize="+getLargeLoadProfilePartitionSize()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() throws IOException {
         return new byte[]{(byte)0xCE,0,0,0,0,0,0,0,0};
    }


    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setSmallLoadProfilePartitionSize(ProtocolUtils.getInt(data,offset++,1));
        setLargeLoadProfilePartitionSize(ProtocolUtils.getInt(data,offset++,1));
    }

    public int getSmallLoadProfilePartitionSize() {
        return smallLoadProfilePartitionSize;
    }

    public void setSmallLoadProfilePartitionSize(int smallLoadProfilePartitionSize) {
        this.smallLoadProfilePartitionSize = smallLoadProfilePartitionSize;
    }

    public int getLargeLoadProfilePartitionSize() {
        return largeLoadProfilePartitionSize;
    }

    public void setLargeLoadProfilePartitionSize(int largeLoadProfilePartitionSize) {
        this.largeLoadProfilePartitionSize = largeLoadProfilePartitionSize;
    }
}
