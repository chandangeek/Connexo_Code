/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.instromet.connection;

import com.energyict.protocolimpl.instromet.core.InstrometProtocol;

public class TableSwitchCommand extends AbstractCommand {
	
	private int tableType;
	
	public TableSwitchCommand(InstrometProtocol instrometProtocol) {
		super(instrometProtocol);
	}
	
	public void setTableType(int type) {
		this.tableType = type;
	}
	
	protected Command preparebuild() {
		Command command = new Command('W');
		command.setStartAddress(0); // only write for table switch
		command.setLength(1); // only write for table switch
		byte[] data = new byte[1];
		data[0] = (byte) tableType;
		command.setData(data);
		return command;
	}

}
