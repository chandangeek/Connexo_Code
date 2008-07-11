package com.energyict.protocolimpl.CM32;

import java.io.IOException;

public class LoginCommand extends AbstractCommand {
	
	private String id;
	private String password;
	
	public LoginCommand(CM32 cm32Protocol) {
		super(cm32Protocol);
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	protected Command preparebuild() {
		Command command = new Command("Login");
		command.addParameter(id);
		command.addParameter(password);
		return command;
	}

}

