package com.energyict.protocolimpl.CM32;


public class LogoutCommand extends AbstractCommand {

	
	public LogoutCommand(CM32 cm32Protocol) {
		super(cm32Protocol);
	}

	protected Command preparebuild() {
		Command command = new Command("Logout");
		return command;
	}

}


