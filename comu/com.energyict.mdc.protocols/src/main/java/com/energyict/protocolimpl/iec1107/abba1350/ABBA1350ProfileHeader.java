/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProfileHeader.java
 *
 */

package com.energyict.protocolimpl.iec1107.abba1350;

import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class ABBA1350ProfileHeader {

	private static final int DEBUG = 0;

	int profileInterval;
    int nrOfChannels;

    /** Creates a new instance of ProfileHeader */
    public ABBA1350ProfileHeader(FlagIEC1107Connection flagIEC1107Connection, int profileNumber) throws IOException {
        if (profileNumber == 1) flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5,"X(;)".getBytes());
        if (profileNumber == 2) flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5,"Z(;)".getBytes());
        byte[] data = flagIEC1107Connection.receiveRawData();
        DataParser dp = new DataParser();
        if ("ERROR".compareTo(dp.parseBetweenBrackets(data,0,0))==0)
            throw new IOException("VDEWProfileHeader, ERROR, possibly requestheader not supported!");
        profileInterval = Integer.parseInt(dp.parseBetweenBrackets(data,0,2))*60;
        nrOfChannels = Integer.parseInt(dp.parseBetweenBrackets(data,0,3));
    }

    /**
     * Getter for property profileInterval.
     * @return Value of property profileInterval.
     */
    public int getProfileInterval() {
        return profileInterval;
    }

    /**
     * Getter for property nrOfChannels.
     * @return Value of property nrOfChannels.
     */
    public int getNrOfChannels() {
        return nrOfChannels;
    }


}
