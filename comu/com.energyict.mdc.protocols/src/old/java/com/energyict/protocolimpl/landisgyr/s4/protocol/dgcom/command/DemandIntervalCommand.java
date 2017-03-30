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

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DemandIntervalCommand extends AbstractCommand {

    private int profileInterval; // in minutes
    int subIntervalInMinutes;
    int type;
    int nrOfSubintervals;

    /** Creates a new instance of TemplateCommand */
    public DemandIntervalCommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandIntervalCommand:\n");
        strBuff.append("   profileInterval="+getProfileInterval()+" minutes\n");
        return strBuff.toString();
    }

    protected byte[] prepareBuild() {
        return new byte[]{(byte)0x0F,0,0,0,0,0,0,0,0};
    }

    protected void parse(byte[] data) throws IOException {
        subIntervalInMinutes = ProtocolUtils.getInt(data,0, 1);
        type = ProtocolUtils.getInt(data,1, 1);
        nrOfSubintervals = ProtocolUtils.getInt(data,2, 1)+1;
        setProfileInterval(subIntervalInMinutes*nrOfSubintervals);
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public int getNrOfIntervalsPerHour() {
        return 60 / getProfileInterval();
    }

    private void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }
}
