package com.energyict.protocolimpl.cm10;
  
public class CommandFactory {
	
	static final int STATUS = 5;
	static final int TIME = 3;
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
	
	public ReadCommand getReadMeterDemandsCommand() {
		return new ReadCommand(cm10Protocol, METER_DEMANDS);
	}
	
	public ReadCommand getReadAlarmTimesCommandCommand() {
		return new ReadCommand(cm10Protocol, ALARM_TIMES);
	}
	
	
}

