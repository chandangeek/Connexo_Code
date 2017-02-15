/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.amco.rtm.core.radiocommand;

public class PortTotalizers {

    private int port;
    private int[] TOUBucketsTotalizers = new int[6];
    private int currentReading;

    public PortTotalizers(int currentReading, int port, int[] TOUBucketsTotalizers) {
        this.currentReading = currentReading;
        this.port = port;
        this.TOUBucketsTotalizers = TOUBucketsTotalizers;
    }

    public int getCurrentReading() {
        return currentReading;
    }

    public int getPort() {
        return port;
    }

    public int[] getTOUBucketsTotalizers() {
        return TOUBucketsTotalizers;
    }
}
