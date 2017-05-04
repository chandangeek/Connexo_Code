/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * HookUp.java
 *
 * Created on 17 mei 2005, 16:59
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class HookUp extends AbstractCommand {

    private static final int DEBUG=0;
    private static final String COMMAND="RH";

    int channelConfig; // binary LSB 8 bit (0=disabled, 1 = enabled)

    /** Creates a new instance of HookUp */
    public HookUp(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
       return "HookUp: 0x"+Integer.toHexString(getChannelConfig());
    }

    public void build() throws IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) {
        if (DEBUG>=1) {
            System.out.println(new String(data));
        }
        String dataStr = new String(data);
        setChannelConfig(Integer.parseInt(dataStr.replaceAll("\r\n",""),16));
    }

    /**
     * Getter for property channelConfig.
     * @return Value of property channelConfig.
     */
    public int getChannelConfig() {
        return channelConfig;
    }

    /**
     * Setter for property channelConfig.
     * @param channelConfig New value of property channelConfig.
     */
    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public int getNrOfChannels() {
        int count=0;
        for (int i=0x0001;i!=0x0100;i<<=1) {
            if ((channelConfig&i)==i) {
                count++;
            }
        }
        return count;
    }

    public boolean isChannelEnabled(int channel) {
        int mask=0x0001<<channel;
        return ((channelConfig&mask)==mask);
    }

}
