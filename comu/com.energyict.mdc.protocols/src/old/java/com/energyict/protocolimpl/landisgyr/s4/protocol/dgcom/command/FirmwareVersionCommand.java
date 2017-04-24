/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * FirmwareVersionCommand.java
 *
 * Created on 22 mei 2006, 15:53
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
public class FirmwareVersionCommand extends AbstractCommand {



    private String productFamily; // bit 7..4 2=DX, 3=RX bit 3..0 major firmware version
    private String firmwareVersion;  // minor firmware version (BCD)
    private float numericFirmwareVersion;
    private String dgcomVersion; // len=2 bytes, version 3.0; len=4 bytes, version 4.05
    /** Creates a new instance of FirmwareVersionCommand */
    public FirmwareVersionCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public boolean isRX() {
        return ("3".compareTo(getProductFamily())==0);
    }
    public boolean isDX() {
        return ("2".compareTo(getProductFamily())==0);
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("FirmwareVersionCommand:\n");
        strBuff.append("   dgcomVersion="+getDgcomVersion()+"\n");
        strBuff.append("   firmwareVersion="+getFirmwareVersion()+"\n");
        strBuff.append("   numericFirmwareVersion="+getNumericFirmwareVersion()+"\n");
        strBuff.append("   productFamily="+getProductFamily()+"\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        return new byte[]{8,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        int len = data.length;

        if (len==2) {
            setProductFamily(""+(data[0]>>4));
            setFirmwareVersion(""+(data[0]&0x0F)+"."+ProtocolUtils.BCD2hex(data[1]));
            setDgcomVersion("3.0");
        }
        else if (len==4) {
            setProductFamily(""+(data[0]>>4));
            setFirmwareVersion(""+(data[0]&0x0F)+"."+ProtocolUtils.BCD2hex(data[1]));
            // 2 reserved bytes
            setDgcomVersion("4.05");
        }
        setNumericFirmwareVersion(Float.parseFloat(getFirmwareVersion()));
    }

    public String getProductFamily() {
        return productFamily;
    }

    private void setProductFamily(String productFamily) {
        this.productFamily = productFamily;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    private void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getDgcomVersion() {
        return dgcomVersion;
    }

    private void setDgcomVersion(String dgcomVersion) {
        this.dgcomVersion = dgcomVersion;
    }

    public float getNumericFirmwareVersion() {
        return numericFirmwareVersion;
    }

    public void setNumericFirmwareVersion(float numericFirmwareVersion) {
        this.numericFirmwareVersion = numericFirmwareVersion;
    }
}
