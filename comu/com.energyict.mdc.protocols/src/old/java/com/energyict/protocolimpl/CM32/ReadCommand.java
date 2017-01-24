package com.energyict.protocolimpl.CM32;

public class ReadCommand extends AbstractCommand {
	
	private int activityCode;
	
	public ReadCommand(CM32 cm32Protocol, int activityCode) {
		super(cm32Protocol);
		this.activityCode = activityCode;
	}
	
	protected Command preparebuild() {
		Command command = new Command(activityCode);
		command.setRead(true);
		return command;
	}

}
