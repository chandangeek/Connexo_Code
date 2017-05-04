/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

public class WriteCommand extends AbstractCommand {
	
	private int activityCode;
	
	public WriteCommand(CM10 cm10Protocol, int activityCode) {
		super(cm10Protocol);
		this.activityCode = activityCode;
	}
	
	protected Command preparebuild() {
		Command command = new Command(activityCode, this.getCM10Protocol().getOutstationId());
		command.setRead(false);
		command.setArguments(getArguments());
		return command;
	}
	

}
