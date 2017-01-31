/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MK10Register.java
 *
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83.registerconfig;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.MeterExceptionInfo;

import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.iskraemeco.mt83.vdew.VDEWRegisterDataParse;

/**
 *
 * @author  jme
 */
public class MT83Registry extends AbstractVDEWRegistry {

	public static final String SERIAL = "MeterSerialNumber";
	public static final String SOFTWARE_REVISION = "SoftwareRevisionNumber";
	public static final String SOFTWARE_DATE = "SoftwareDate";
	public static final String DEVICE_TYPE = "DeviceType";
    public static final String BATTERY_HOURS = "BatteryHours";
    public static final String LAST_CONFIG_CHANGE_DATE = "LastConfigDate";

	public static final String PROFILE_INTERVAL = "ProfileInterval";

	public static final String TIME_AND_DATE_STRING = "TimeDateString";
	public static final String TIME_AND_DATE_READWRITE = "TimeDateReadWrite";
	public static final String TIME_AND_DATE_READONLY = "TimeDateReadOnly";
	public static final String TIME_READONLY = "TimeReadOnly";
	public static final String DATE_READONLY = "DateReadOnly";

	public static final String BILLING_RESET_COUNTER = "BillingResetCounter";

	public static final String BILLING_DATE_START = "1.0.0.1.2.0";
	public static final String BILLING_DATE_1 = "1.0.0.1.2.1";
	public static final String BILLING_DATE_2 = "1.0.0.1.2.2";
	public static final String BILLING_DATE_3 = "1.0.0.1.2.4";
	public static final String BILLING_DATE_4 = "1.0.0.1.2.4";
	public static final String BILLING_DATE_5 = "1.0.0.1.2.5";
	public static final String BILLING_DATE_6 = "1.0.0.1.2.6";
	public static final String BILLING_DATE_7 = "1.0.0.1.2.7";
	public static final String BILLING_DATE_8 = "1.0.0.1.2.8";
	public static final String BILLING_DATE_9 = "1.0.0.1.2.9";
	public static final String BILLING_DATE_10 = "1.0.0.1.2.10";
	public static final String BILLING_DATE_11 = "1.0.0.1.2.11";
	public static final String BILLING_DATE_12 = "1.0.0.1.2.12";
	public static final String BILLING_DATE_13 = "1.0.0.1.2.13";
	public static final String BILLING_DATE_14 = "1.0.0.1.2.14";
	public static final String BILLING_DATE_15 = "1.0.0.1.2.15";

    public static final String BILLING_RESET_COMMAND = "0.0.5.0.4.0";

    /** Creates a new instance of MK10Register */
    public MT83Registry(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink) {
        super(meterExceptionInfo,protocolLink);
    }

	protected void initRegisters() {
		final byte[] READ1 = FlagIEC1107Connection.READ1;
		final byte[] READ5 = FlagIEC1107Connection.READ5;
		final byte[] READ6 = FlagIEC1107Connection.READ6;
		final byte[] WRITE1 = FlagIEC1107Connection.WRITE1;
		final byte[] WRITE5 = FlagIEC1107Connection.WRITE5;
        final byte[] COMMAND = FlagIEC1107Connection.EXECUTE_COMMAND;

		final int STRING = VDEWRegisterDataParse.VDEW_STRING;
		final int INTEGER = VDEWRegisterDataParse.VDEW_INTEGER;
		final int DATETIME = VDEWRegisterDataParse.VDEW_DATETIME;
		final int DATETIME_NOSEC = VDEWRegisterDataParse.VDEW_DATETIME_NOSEC;
		final int TIME_HHMMSS = VDEWRegisterDataParse.VDEW_TIME_HHMMSS;
		final int DATE_YYMMDD = VDEWRegisterDataParse.VDEW_DATE_YYMMDD;

		// when READ5 is invoked on 11 and 12, SHHMMSS and SYYMMDD are returned S = seasonal info 0=normal, 1=DST, 2=UTC
        // when READ1 is invoked on 11 and 12, HHMMSS and YYMMDD are returned

        registers.put(SERIAL, newReg("1.0.0.0.0.255", STRING, false, null, READ1, null));
        registers.put(SOFTWARE_REVISION, newReg("1.0.0.2.0.255", STRING, false, null, READ1, null));
        registers.put(SOFTWARE_DATE, newReg("0.0.96.1.3.255", STRING, false, null, READ1, null));
        registers.put(DEVICE_TYPE, newReg("0.0.96.1.1.255", STRING, false, null, READ1, null));


        registers.put(PROFILE_INTERVAL, newReg("1.0.0.8.5.255", INTEGER, false, null, READ1, null));

        registers.put(TIME_AND_DATE_STRING, newReg("1.0.0.9.4.255", STRING, false, null, READ1, null));
        registers.put(TIME_AND_DATE_READONLY, newReg("1.0.0.9.4.255", DATETIME, false, null, READ1, null));
        registers.put(TIME_AND_DATE_READWRITE, newReg("1.0.0.9.4.255",DATETIME, true, null, READ1, WRITE1));

        registers.put(TIME_READONLY, newReg("1.0.0.9.1.255", TIME_HHMMSS, false, null, READ1, null));
        registers.put(DATE_READONLY, newReg("1.0.0.9.2.255",DATE_YYMMDD, true, null, READ1, WRITE1));


        registers.put(BILLING_RESET_COUNTER, newReg("1.0.0.1.0.255", INTEGER, false, null, READ1, null));

        registers.put(BILLING_DATE_1, newReg("1.0.0.1.2.1", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_2, newReg("1.0.0.1.2.2", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_3, newReg("1.0.0.1.2.3", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_4, newReg("1.0.0.1.2.4", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_5, newReg("1.0.0.1.2.5", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_6, newReg("1.0.0.1.2.6", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_7, newReg("1.0.0.1.2.7", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_8, newReg("1.0.0.1.2.8", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_9, newReg("1.0.0.1.2.9", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_10, newReg("1.0.0.1.2.10", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_11, newReg("1.0.0.1.2.11", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_12, newReg("1.0.0.1.2.12", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_13, newReg("1.0.0.1.2.13", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_14, newReg("1.0.0.1.2.14", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
        registers.put(BILLING_DATE_15, newReg("1.0.0.1.2.15", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));

        registers.put(BATTERY_HOURS, newReg("0.0.96.6.0.255", STRING, VDEWRegister.CACHED, false, Unit.get(BaseUnit.HOUR), READ1, null));
        registers.put(BILLING_RESET_COMMAND, newReg("0.0.5.0.4.0", STRING, VDEWRegister.NOT_CACHED, true, Unit.getUndefined(), null, COMMAND));
        registers.put(LAST_CONFIG_CHANGE_DATE, newReg("0.0.C.2.1", DATETIME_NOSEC, VDEWRegister.CACHED, false, null, READ1, null));
    }

    private VDEWRegister newReg(String registerID_in, int dataType, boolean isWritable, Unit unit, byte[] readCommand, byte[] writeCommand) {
    	if (!isWritable) writeCommand = null;
    	return new VDEWRegister(registerID_in, dataType,0, -1, unit, isWritable, VDEWRegister.NOT_CACHED, readCommand, writeCommand, true);
    }

    private VDEWRegister newReg(String registerID_in, int dataType, boolean isCached, boolean isWritable, Unit unit, byte[] readCommand, byte[] writeCommand) {
    	if (!isWritable) writeCommand = null;
    	return new VDEWRegister(registerID_in, dataType,0, -1, unit, isWritable, isCached, readCommand, writeCommand, true);
    }

}
