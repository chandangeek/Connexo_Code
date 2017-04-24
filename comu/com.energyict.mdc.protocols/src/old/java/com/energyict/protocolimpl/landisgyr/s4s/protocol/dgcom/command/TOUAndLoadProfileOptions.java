/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
 * @author Koen
 */
public class TOUAndLoadProfileOptions extends AbstractCommand {

    /*
        byte 0
        bit0 Binary Demand only mode, if set
        bit1 Binary TOU capable, if set
        bit2 Binary Load profile enabled, if set
        bit3 Binary Load profile capable, if set
        bit4 Binary Normal memory size (32K)
        bit5 Binary Expanded memory size (128K)
        bit6 Binary kWh only, if set (DX only, not used prior to 2.00)
        bit7 Binary 512K memory size (RX only)

        byte 1 & 2
        BCD Boot signature 12 34
    */

    private int optionalFeatures;

    /** Creates a new instance of TemplateCommand */
    public TOUAndLoadProfileOptions(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TOUAndLoadProfileOptions:\n");
        strBuff.append("   bit0 Binary Demand only mode, if set\n   bit1 Binary TOU capable, if set\n   bit2 Binary Load profile enabled, if set\n   bit3 Binary Load profile capable, if set\n   bit4 Binary Normal memory size (32K)\n   bit5 Binary Expanded memory size (128K)\n   bit6 Binary kWh only, if set (DX only, not used prior to 2.00)\n   bit7 Binary 512K memory size (RX only)\n");
        strBuff.append("   optionalFeatures="+Integer.toHexString(getOptionalFeatures())+"\n");
        return strBuff.toString();
    }

    public boolean is128KMemory() {
        return (getOptionalFeatures()&0x20) == 0x20;
    }

    public boolean isLoadProfileActive() {
        return (getOptionalFeatures()&0x0C) == 0x0C;
    }

    protected byte[] prepareBuild() {
        return new byte[]{(byte)0x5A,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        setOptionalFeatures(ProtocolUtils.getInt(data,0, 1));
        // byte 1 and 2 = boot signature BCD 1234

    }

    public int getOptionalFeatures() {
        return optionalFeatures;
    }

    private void setOptionalFeatures(int optionalFeatures) {
        this.optionalFeatures = optionalFeatures;
    }
}
