/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.instromet.v444;

import com.energyict.protocolimpl.instromet.connection.LogoffCommand;
import com.energyict.protocolimpl.instromet.connection.ReadCommand;
import com.energyict.protocolimpl.instromet.connection.TableSwitchCommand;
import com.energyict.protocolimpl.instromet.connection.WriteCommand;
import com.energyict.protocolimpl.instromet.v444.tables.CorrectorInformationTable;
import com.energyict.protocolimpl.instromet.v444.tables.CountersTable;
import com.energyict.protocolimpl.instromet.v444.tables.LoggedDataTable;
import com.energyict.protocolimpl.instromet.v444.tables.LoggingConfigurationTable;
import com.energyict.protocolimpl.instromet.v444.tables.PeakHourPeakDayTable;

import java.util.Calendar;

public class CommandFactory {

	private Instromet444 instromet444;

	public CommandFactory(Instromet444 instromet444) {
		this.instromet444 = instromet444;
	}

	protected TableSwitchCommand tableSwitchCommand(int tableType) {
		TableSwitchCommand writeCommand = new TableSwitchCommand(instromet444);
		writeCommand.setTableType(tableType);
		return writeCommand;
	}

	public ReadCommand readLoggedDataCommand(int startAddress, int size) {
		return readCommand(startAddress, size);
	}

	public TableSwitchCommand switchToLoggedDataCommand() {
		return tableSwitchCommand(
				new LoggedDataTable(
						instromet444.getTableFactory()).getTableType());
	}

	public TableSwitchCommand switchToLoggingConfigurationCommand() {
		System.out.println("send table switch logging conf");
		return tableSwitchCommand(
				new LoggingConfigurationTable(
						instromet444.getTableFactory()).getTableType());
	}

	public TableSwitchCommand switchToCorrectorInformation() {
		return tableSwitchCommand(
				new CorrectorInformationTable(
						instromet444.getTableFactory()).getTableType());
	}


	public TableSwitchCommand switchToPeakTable() {
		return tableSwitchCommand(
				new PeakHourPeakDayTable(
						instromet444.getTableFactory()).getTableType());
	}

	public TableSwitchCommand switchToCounters() {
		return tableSwitchCommand(
				new CountersTable(
						instromet444.getTableFactory()).getTableType());
	}

	public LogoffCommand logoffCommand() {
		return new LogoffCommand(instromet444);
	}

	public ReadCommand readHeadersCommand() {
		return readCommand(1, 5);
	}

	protected ReadCommand readCommand(int startAddress, int length) {
		ReadCommand readCommand = new ReadCommand(instromet444);
		readCommand.setStartAddress(startAddress);
		readCommand.setLength(length);
		return readCommand;
	}

	public WriteCommand setTimeCommand() {
		System.out.println("setTimeCommand");
		int startAddress = 28;
		byte[] data = new byte[7];
		Calendar cal = Calendar.getInstance(instromet444.getTimeZone());

		cal.add(Calendar.MILLISECOND,this.instromet444.getRoundtripCorrection());

		int year = cal.get(Calendar.YEAR) - 2000;
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		int sec = cal.get(Calendar.SECOND);
		int weekDay;
		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == Calendar.SUNDAY)
			weekDay = 1;
		else if (dayOfWeek == Calendar.MONDAY)
			weekDay = 2;
		else if (dayOfWeek == Calendar.TUESDAY)
			weekDay = 3;
		else if (dayOfWeek == Calendar.WEDNESDAY)
			weekDay = 4;
		else if (dayOfWeek == Calendar.THURSDAY)
			weekDay = 5;
		else if (dayOfWeek == Calendar.FRIDAY)
			weekDay = 6;
		else // saterday
			weekDay = 7;

		data[6] = (byte) (year&0xFF);
		data[5] = (byte) (month&0xFF);
		data[4] = (byte) (day&0xFF);
		data[3] = (byte) (weekDay&0xFF);
		data[2] = (byte) (hour&0xFF);
		data[1] = (byte) (min&0xFF);
		data[0] = (byte) (sec&0xFF);
		return writeCommand(startAddress, data);
	}

	public WriteCommand writeCommand(int startAddress, byte[] data) {
		WriteCommand writeCommand = new WriteCommand(instromet444);
		writeCommand.setStartAddress(startAddress);
		writeCommand.setData(data);
		return writeCommand;
	}

	public ReadCommand readLogSelectorCommand() {
		return readCommand(9, 10);
	}

	public ReadCommand readCountersCommand() {
		return readCommand(6, 16);
	}

	public ReadCommand readCorrectorInformationCommand() {
		return readCommand(6, 29);
	}

	public ReadCommand readPeakCommand() {
		return readCommand(6, 15);
	}




}

