package com.energyict.protocolimpl.cm10;

import com.energyict.protocol.ProtocolUtils;
  
public class CommandFactory {
	
	static final int STATUS = 5;
	static final int TIME = 3;
	static final int TRIM_TIME = 4;
	static final int FULL_PERSONALITY_TABLE = 1;
	static final int CURRENT_DIAL_READINGS = 10;
	static final int METER_DEMANDS = 7;
	static final int ALARM_TIMES = 15;
	
	private CM10 cm10Protocol;
	
	public CommandFactory(CM10 cm10Protocol) {
		this.cm10Protocol = cm10Protocol;
	}
	
	public ReadCommand getReadStatusCommand() {
		return new ReadCommand(cm10Protocol, STATUS);
	}
	
	public ReadCommand getReadTimeCommand() {
		return new ReadCommand(cm10Protocol, TIME);
	}
	
	
	public ReadCommand getReadFullPersonalityTableCommand() {
		return new ReadCommand(cm10Protocol, FULL_PERSONALITY_TABLE);
	}
	
	public ReadCommand getReadCurrentDialReadingsCommand() {
		return new ReadCommand(cm10Protocol, CURRENT_DIAL_READINGS);
	}
	
	public ReadCommand getReadMeterDemandsCommand(int stPeriod, int noHHours) {
		ReadCommand readCommand = new ReadCommand(cm10Protocol, METER_DEMANDS);
		readCommand.setArguments(getMeterDemandsArguments(stPeriod, noHHours));
		return readCommand;
	}
	
	protected byte[] getMeterDemandsArguments(int stPeriod, int noHHours) {
		byte[] args = new byte[4];
		args[0] = (byte) (stPeriod & 0xFF);
		args[1] = (byte) ((stPeriod>>8)&0xFF);
		args[2] = (byte) (noHHours & 0xFF);
		args[3] = (byte) ((noHHours>>8)&0xFF);
		cm10Protocol.getLogger().info("args meter demands: " + ProtocolUtils.outputHexString(args));
		return args;
	}
	
	public ReadCommand getReadAlarmTimesCommandCommand() {
		return new ReadCommand(cm10Protocol, ALARM_TIMES);
	}
	
	public WriteCommand getTrimClockCommand(byte secondsToTrim) {
		WriteCommand writeCommand = new WriteCommand(cm10Protocol, TRIM_TIME);
		byte[] args = new byte[1];
		args[0] = secondsToTrim;
		cm10Protocol.getLogger().info("args trim time: " + ProtocolUtils.outputHexString(args));
		writeCommand.setArguments(args);
		return writeCommand;
	}
	
	
}

