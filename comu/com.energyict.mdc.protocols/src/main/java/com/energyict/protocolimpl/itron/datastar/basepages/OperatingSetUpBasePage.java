/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * OperatingSetUpBasePage.java
 *
 * Created on 12 september 2006, 16:47
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.datastar.basepages;

import com.energyict.protocolimpl.itron.protocol.AbstractBasePage;
import com.energyict.protocolimpl.itron.protocol.BasePageDescriptor;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class OperatingSetUpBasePage extends AbstractBasePage {

    private boolean dstEnabled;
    private int nrOfChannels;
    private int profileInterval; // in minutes


    /** Creates a new instance of OperatingSetUpBasePage */
    public OperatingSetUpBasePage(BasePagesFactory basePagesFactory) {
        super(basePagesFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("OperatingSetUpBasePage:\n");
        strBuff.append("   nrOfChannels="+getNrOfChannels()+"\n");
        strBuff.append("   profileInterval="+getProfileInterval()+"\n");
        strBuff.append("   dstEnabled="+isDstEnabled()+"\n");
        return strBuff.toString();
    }

    protected BasePageDescriptor preparebuild() throws IOException {
        return new BasePageDescriptor(0x6e, 1);
    }

    protected void parse(byte[] data) throws IOException {
        setDstEnabled((((int)data[0]&0xff) & 0x02)==0x02);

        int temp = (((int)data[0]&0xff) >> 2) & 0x03;
        if (temp == 0) setNrOfChannels(0);
        if (temp == 1) setNrOfChannels(4);
        if (temp == 2) setNrOfChannels(2);
        if (temp == 3) setNrOfChannels(1);

        temp = (((int)data[0]&0xff) >> 4) & 0x07;
        if (temp == 0) setProfileInterval(60);
        if (temp == 1) setProfileInterval(30);
        if (temp == 2) setProfileInterval(15);
        if (temp == 3) setProfileInterval(12);
        if (temp == 4) setProfileInterval(10);
        if (temp == 5) setProfileInterval(5);
        if (temp == 6) setProfileInterval(2);
        if (temp == 7) setProfileInterval(1);
    }

    public boolean isDstEnabled() {
        return dstEnabled;
    }

    public void setDstEnabled(boolean dstEnabled) {
        this.dstEnabled = dstEnabled;
    }

    public int getNrOfChannels() {
        return nrOfChannels;
    }

    public void setNrOfChannels(int nrOfChannels) {
        this.nrOfChannels = nrOfChannels;
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }




} // public class RealTimeBasePage extends AbstractBasePage
