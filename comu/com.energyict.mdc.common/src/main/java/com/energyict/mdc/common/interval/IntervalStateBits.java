/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * IntervalStateBits.java
 *
 * Created on 16 juli 2003, 15:29
 */

package com.energyict.mdc.common.interval;

/**
 * Defines interval state bit constants
 *
 * @author Karel
 * @deprecated use {@link com.elster.jupiter.metering.readings.ReadingQuality}s instead
 */
@Deprecated
public interface IntervalStateBits {

    /**
     * OK status bit
     */
    int OK = 0;
    /**
     * power down status bit
     */
    int POWERDOWN = 1;
    /**
     * power up status bit
     */
    int POWERUP = 2;
    /**
     * short/long interval status bit
     */
    int SHORTLONG = 4;
    /**
     * watchdof reset occured status bit
     */
    int WATCHDOGRESET = 8;
    /**
     * configuration changed status bit
     */
    int CONFIGURATIONCHANGE = 16;
    /**
     * corrupted interval data
     */
    int CORRUPTED = 32;
    /**
     * value overflow
     */
    int OVERFLOW = 64;
    /**
     * estimated value
     */
    int ESTIMATED = 128;
    /**
     * missing value
     */
    int MISSING = 256;
    /**
     * modified value
     */
    int MODIFIED = 512;
    /**
     * revised value
     */
    int REVISED = 1024;
    /**
     * other value
     */
    int OTHER = 2048;
    /**
     * exporting energy
     */
    int REVERSERUN = 4096;
    /**
     * one ore more phases missing
     */
    int PHASEFAILURE = 8192;
    /**
     * time difference exceeds maximum
     */
    int BADTIME = 16384;
    /**
     * initial validation failed state
     */
    int INITIALFAILVALIDATION = 32768;
    /**
     * current valdation failed state
     */
    int CURRENTFAILVALIDATION = 65536;
    /**
     * device error of any kind
     */
    int DEVICE_ERROR = 0x20000;
    /**
     * device backup battery low
     */
    int BATTERY_LOW = 0x40000;

    /**
     * meter in test state
     */
    int TEST = 0x80000;


    /**
     * String array of the state bits String representation
     */
    String[] states = {"PD", "PU", "S/L", "WRes",
            "conf", "corrupt", "ovf",
            "est", "miss", "mod", "rev",
            "*", "revrun", "phfail", "badtime", "i.f.v.", "c.f.v.", "deverr", "battlow", "test"};

    // KV 29082006 changed
    /**
     * All state bits OR-ed together
     */
    int ANY =
            POWERDOWN |
                    POWERUP |
                    SHORTLONG |
                    WATCHDOGRESET |
                    CONFIGURATIONCHANGE |
                    CORRUPTED |
                    OVERFLOW |
                    ESTIMATED |
                    MISSING |
                    MODIFIED |
                    REVISED |
                    OTHER |
                    REVERSERUN |
                    PHASEFAILURE |
                    BADTIME |
                    INITIALFAILVALIDATION |
                    CURRENTFAILVALIDATION |
                    DEVICE_ERROR |
                    BATTERY_LOW |
                    TEST;


}
