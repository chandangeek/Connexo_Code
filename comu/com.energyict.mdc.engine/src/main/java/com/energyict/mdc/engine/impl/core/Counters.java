/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.util.Counter;

/**
 * Bundles a number of {@link Counter}s that will track
 * number of bytes read/sent and number of packets read/sent.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-30 (16:22)
 */
public class Counters {

    private Counter bytesRead = com.elster.jupiter.util.Counters.newStrictThreadSafeCounter();
    private Counter bytesSent = com.elster.jupiter.util.Counters.newStrictThreadSafeCounter();
    private Counter packetsRead = com.elster.jupiter.util.Counters.newStrictThreadSafeCounter();
    private Counter packetsSent = com.elster.jupiter.util.Counters.newStrictThreadSafeCounter();

    private boolean reading = false;
    private boolean writing = false;

    public int getBytesRead () {
        return bytesRead.getValue();
    }

    public void bytesRead (int value) {
        this.bytesRead.add(value);
    }

    public void resetBytesRead () {
        this.bytesRead.reset();
    }

    public int getBytesSent () {
        return bytesSent.getValue();
    }

    public void bytesSent (int value) {
        this.bytesSent.add(value);
    }

    public void resetBytesSent () {
        this.bytesSent.reset();
    }

    public int getPacketsRead () {
        return packetsRead.getValue();
    }

    public void packetRead () {
        this.packetsRead.increment();
    }

    public void resetPacketsRead () {
        this.packetsRead.reset();
    }

    public int getPacketsSent () {
        return packetsSent.getValue();
    }

    public void packetSent () {
        this.packetsSent.increment();
    }

    public void resetPacketsSent () {
        this.packetsSent.reset();
    }

    public void reading(){
        this.reading = true;
        this.writing = false;
    }

    public void writing(){
        this.reading = false;
        this.writing = true;
    }

    public boolean isReading(){
        return this.reading;
    }

    public boolean isWriting(){
        return this.writing;
    }

}