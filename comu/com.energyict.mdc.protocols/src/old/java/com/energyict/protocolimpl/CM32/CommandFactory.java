/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.CM32;
  
public class CommandFactory {
	
	static final int STATUS = 5;
	static final int TIME = 3;
	static final int FULL_PERSONALITY_TABLE = 1;
	static final int CURRENT_DIAL_READINGS = 10;
	static final int METER_DEMANDS = 7;
	static final int ALARM_TIMES = 15;
	
	private CM32 cm32Protocol;
	
	public CommandFactory(CM32 cm32Protocol) {
		this.cm32Protocol = cm32Protocol;
	}
	
	public ReadCommand getReadStatusCommand() {
		return new ReadCommand(cm32Protocol, STATUS);
	}
	
	public ReadCommand getReadTimeCommand() {
		return new ReadCommand(cm32Protocol, TIME);
	}
	
	public ReadCommand getReadFullPersonalityTableCommand() {
		return new ReadCommand(cm32Protocol, FULL_PERSONALITY_TABLE);
	}
	
	public ReadCommand getReadCurrentDialReadingsCommand() {
		return new ReadCommand(cm32Protocol, CURRENT_DIAL_READINGS);
	}
	
	public ReadCommand getReadMeterDemandsCommand() {
		return new ReadCommand(cm32Protocol, METER_DEMANDS);
	}
	
	public ReadCommand getReadAlarmTimesCommandCommand() {
		return new ReadCommand(cm32Protocol, ALARM_TIMES);
	}
	
	
}

