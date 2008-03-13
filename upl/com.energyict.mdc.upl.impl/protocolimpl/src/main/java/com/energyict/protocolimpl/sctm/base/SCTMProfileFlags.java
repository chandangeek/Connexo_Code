/*
 * SCTMProfileFlags.java
 *
 * Created on 9 maart 2005, 17:19
 */

package com.energyict.protocolimpl.sctm.base;

/**
 *
 * @author  Koen
 */
public interface SCTMProfileFlags {
    // value status bits
    public final int MODIFIEDVALUE = 0x80; // value manually modified
    public final int CORRUPTEDVALUE = 0x40; // corrupted value if not U, NP or A
    public final int VALUEOVERFLOW = 0x10; // overflowed value
    public final int TESTBIT = 0x08; // test

    // device status bits
    public final int T_BIT =  0x8000; // time set, DST switch
    public final int U_BIT =  0x4000; // short long interval caused by power outage or time set
    public final int M_BIT =  0x2000; // parameter or data modified
    public final int A_BIT =  0x1000; // general alarm or tariff switching (metcom2/3)
    public final int S_BIT =  0x0800; // DST active
    public final int TS_BIT = 0x0400; // test bit
    public final int NP_BIT = 0x0200; // no power for the whole integration period. Zero fill
    public final int AL_BIT = 0x0100; // Error occured during selftest (metcom2)
    public final int F_BIT =  0x0080; // crc error in integration data block
}
