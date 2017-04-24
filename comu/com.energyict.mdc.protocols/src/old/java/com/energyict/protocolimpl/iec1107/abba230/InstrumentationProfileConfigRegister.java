/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class InstrumentationProfileConfigRegister implements ProfileConfigRegister {

    private ABBA230RegisterFactory rFactory;
    private byte[] allChannelMask = null;
    private ArrayList register = new ArrayList();
    private ArrayList channelInfo = new ArrayList();

    private int[] channelValueConfigurations = new int[8];

    private String phaseConfigurations[] = {"System", "Phase A", "Phase B", "Phase C"};
    private String storageConfigurations[] = {"Average", "Maximum", "Minimum", "Last"};

    public void loadConfig(ABBA230RegisterFactory rFactory, byte[] data) throws IOException {
        this.rFactory = rFactory;
        this.allChannelMask = data.clone();
        init();
    }

    public void loadConfig(ABBA230RegisterFactory rFactory, int data ) throws IOException {
        this.rFactory = rFactory;
        byte b1 = (byte)((data&0xFF00)>>8);
        byte b2 = (byte)(data&0x00FF);
        byte [] ba = { b1, b2 };
        this.allChannelMask = ba;
        init();
    }

    private void init() throws IOException {
        int i = 0;

        if (allChannelMask.length == 16) {
            for (int chn = 0; chn < 16; chn+=2) {
                byte[] channelMask = ProtocolTools.getSubArray(allChannelMask, chn, chn + 2);
                int valueConfiguration = (channelMask[1] & 0x0F);
                int phaseConfiguration = (channelMask[1] & 0x70);
                int storageConfiguration = (channelMask[0] & 0x03);
                channelValueConfigurations[chn/2] = valueConfiguration;

                if (valueConfiguration != 0) {
                    ABBA230Register r = rFactory.getInstrumentationChannelRegister(valueConfiguration, phaseConfigurations[phaseConfiguration], storageConfigurations[storageConfiguration]);
                    register.add(r);
                    channelInfo.add(new ChannelInfo(i, "ELSTERAS230_InstrumentationChannel_" + i, r.getUnit()));
                    i = i + 1;
                }
            }
        }
    }

    public int getNumberRegisters() {
        return register.size();
    }

    public int[] getChannelValueConfigurations() {
        return channelValueConfigurations;
    }

    public Collection getRegisters(){
        return register;
    }

    public String getAllChannelMaskString(){
        return allChannelMask.toString();
    }

    public byte[] getAllChannelMask() {
        return allChannelMask;
    }

    public Collection toChannelInfo() throws IOException {
        return channelInfo;
    }

    public String toShortString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( " [" );
        Iterator i = register.iterator();
        while( i.hasNext() ) {
            ABBA230Register r = (ABBA230Register)i.next();
            rslt.append( r.getName() + " " );
        }
        rslt.append( "] " );
        return rslt.toString();
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append("InstrumentationProfileConfigRegister [");
        rslt.append(" chn msk: " + getAllChannelMaskString());
        try {
            Iterator i = this.toChannelInfo().iterator();
            while( i.hasNext() ){
                ChannelInfo ci = (ChannelInfo)i.next();
                rslt.append( "ChannelInfo [" + ci.getId() + " " + ci.getName() + " " + ci.getChannelId() + " ]\n" );
            }
        } catch( IOException ioe ){
            rslt.append( ioe );
        }
        rslt.append( "]" );
        return rslt.toString();
    }
}