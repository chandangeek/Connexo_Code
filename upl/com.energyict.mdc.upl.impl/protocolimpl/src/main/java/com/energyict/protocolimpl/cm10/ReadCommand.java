package com.energyict.protocolimpl.cm10;

public class ReadCommand extends AbstractCommand {
	
	private int activityCode;
	
	public ReadCommand(CM10 cm10Protocol, int activityCode) {
		super(cm10Protocol);
		this.activityCode = activityCode;
	}
	
	protected Command preparebuild() {
		Command command = new Command(activityCode, this.getCM10Protocol().getOustationId());
		command.setRead(true);
		return command;
	}

}
