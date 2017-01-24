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

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author jme
 */
public class S4sConfiguration extends AbstractCommand {
    /*
bit
0 Binary Register is in AXL configuration, if set
1 Binary Register is in AX configuration, if set
2 Binary Single Phase Service expected, if set
3 Binary Meter is an ZMC configuration, if set
4 Binary S4-3 daughterboard installed, if set
5 Binary S4-4 daughterboard installed, if set
6 Binary 128K RAM board installed, if set
7 Binary 512K RAM board installed, if set
    */

    private int config;

    /** Creates a new instance of TemplateCommand */
    public S4sConfiguration(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("S4sConfiguration:\n");
        strBuff.append("   bit0 Binary Register is in AXL configuration, if set\n   bit1 Binary Register is in AX configuration, if set\n   bit2 Binary Single Phase Service expected, if set\n   bit3 Binary Meter is an ZMC configuration, if set\n   bit4 Binary S4-3 daughterboard installed, if set\n   bit5 Binary S4-4 daughterboard installed, if set\n   bit6 Binary 128K RAM board installed, if set\n   bit7 Binary 512K RAM board installed, if set\n");
        strBuff.append("   config=0x"+Integer.toHexString(getConfig())+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        return new byte[]{(byte)0x8F,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        setConfig(ProtocolUtils.getInt(data,0, 1));

    }

    public int getConfig() {
        return config;
    }

    private void setConfig(int config) {
        this.config = config;
    }
}
