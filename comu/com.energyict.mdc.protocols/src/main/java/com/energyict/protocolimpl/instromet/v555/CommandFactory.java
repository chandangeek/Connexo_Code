package com.energyict.protocolimpl.instromet.v555;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.instromet.connection.LogoffCommand;
import com.energyict.protocolimpl.instromet.connection.ReadCommand;
import com.energyict.protocolimpl.instromet.connection.TableSwitchCommand;
import com.energyict.protocolimpl.instromet.connection.WriteCommand;
import com.energyict.protocolimpl.instromet.v555.tables.CorrectorInformationTable;
import com.energyict.protocolimpl.instromet.v555.tables.CountersTable;
import com.energyict.protocolimpl.instromet.v555.tables.LoggedDataTable;
import com.energyict.protocolimpl.instromet.v555.tables.LoggingConfigurationTable;
import com.energyict.protocolimpl.instromet.v555.tables.PeakHourPeakDayTable;

import java.util.Calendar;

public class CommandFactory {

	private Instromet555 instromet555;

	public CommandFactory(Instromet555 instromet555) {
		this.instromet555 = instromet555;
	}

	protected TableSwitchCommand tableSwitchCommand(int tableType) {
		TableSwitchCommand writeCommand = new TableSwitchCommand(instromet555);
		writeCommand.setTableType(tableType);
		return writeCommand;
	}

	public ReadCommand readLoggedDataCommand(int startAddress, int size) {
		return readCommand(startAddress, size);
	}

	public TableSwitchCommand switchToLoggedDataCommand() {
		return tableSwitchCommand(
				new LoggedDataTable(
						instromet555.getTableFactory()).getTableType());
	}

	public TableSwitchCommand switchToLoggingConfigurationCommand() {
		System.out.println("send table switch logging conf");
		return tableSwitchCommand(
				new LoggingConfigurationTable(
						instromet555.getTableFactory()).getTableType());
	}

	public TableSwitchCommand switchToCorrectorInformation() {
		return tableSwitchCommand(
				new CorrectorInformationTable(
						instromet555.getTableFactory()).getTableType());
	}


	public TableSwitchCommand switchToPeakTable() {
		return tableSwitchCommand(
				new PeakHourPeakDayTable(
						instromet555.getTableFactory()).getTableType());
	}

	public TableSwitchCommand switchToCounters() {
		return tableSwitchCommand(
				new CountersTable(
						instromet555.getTableFactory()).getTableType());
	}

	public LogoffCommand logoffCommand() {
		return new LogoffCommand(instromet555);
	}

	public ReadCommand readHeadersCommand() {
		return readCommand(1, 5);
	}

	protected ReadCommand readCommand(int startAddress, int length) {
		ReadCommand readCommand = new ReadCommand(instromet555);
		readCommand.setStartAddress(startAddress);
		readCommand.setLength(length);
		return readCommand;
	}

	public WriteCommand setTimeCommand() {
		int startAddress = 28;
		byte[] data = new byte[7];
		Calendar cal = Calendar.getInstance(instromet555.getTimeZone());

		cal.add(Calendar.MILLISECOND,this.instromet555.getRoundtripCorrection());

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
		data[6] = ProtocolUtils.hex2BCD(year);
		data[5] = ProtocolUtils.hex2BCD(month);
		data[4] = ProtocolUtils.hex2BCD(day);
		data[3] = ProtocolUtils.hex2BCD(weekDay);
		data[2] = ProtocolUtils.hex2BCD(hour);
		data[1] = ProtocolUtils.hex2BCD(min);
		data[0] = ProtocolUtils.hex2BCD(sec);
		return writeCommand(startAddress, data);
	}

	public WriteCommand writeCommand(int startAddress, byte[] data) {
		WriteCommand writeCommand = new WriteCommand(instromet555);
		writeCommand.setStartAddress(startAddress);
		writeCommand.setData(data);
		return writeCommand;
	}

	public ReadCommand readLogSelectorCommand() {
		return readCommand(9, 6);
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

