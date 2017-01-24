/*
 * SCTMProfileFlags.java
 *
 * Created on 9 maart 2005, 17:19
 */

package com.energyict.protocolimpl.sctm.base;

/**
 * 
 * @author Koen
 */
public interface SCTMProfileFlags {

	// value status bits
	int MODIFIEDVALUE = 0x80; // value manually modified
	int CORRUPTEDVALUE = 0x40; // corrupted value if not U, NP or A
	int VALUEOVERFLOW = 0x10; // overflowed value
	int TESTBIT = 0x08; // test

	// device status bits
	int T_BIT = 0x8000; // time set, DST switch
	int U_BIT = 0x4000; // short long interval caused by power outage or time set
	int M_BIT = 0x2000; // parameter or data modified
	int A_BIT = 0x1000; // general alarm or tariff switching (metcom2/3)
	int S_BIT = 0x0800; // DST active
	int TS_BIT = 0x0400; // test bit
	int NP_BIT = 0x0200; // no power for the whole integration period. Zero fill
	int AL_BIT = 0x0100; // Error occured during selftest (metcom2)
	int F_BIT = 0x0080; // crc error in integration data block

}
