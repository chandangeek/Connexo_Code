/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.cm10;

public class ReadCommand extends AbstractCommand {
	
	private int activityCode;
	private boolean sendAck = true;
	
	public ReadCommand(CM10 cm10Protocol, int activityCode) {
		super(cm10Protocol);
		this.activityCode = activityCode;
	}
	
	public void sendAck(boolean value) {
		sendAck = value;
	}
	
	protected Command preparebuild() {
		Command command = new Command(activityCode, this.getCM10Protocol().getOutstationId());
		command.setRead(true);
		command.setArguments(getArguments());
		command.setSendAckAfterThisCommand(sendAck);
		return command;
	}
	

}
