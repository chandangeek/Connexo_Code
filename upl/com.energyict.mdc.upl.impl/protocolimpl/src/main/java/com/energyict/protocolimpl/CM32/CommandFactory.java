package com.energyict.protocolimpl.CM32;

import com.energyict.protocolimpl.instromet.connection.ReadCommand;
  
public class CommandFactory {
	
	private CM32 cm32Protocol;
	
	public CommandFactory(CM32 cm32Protocol) {
		this.cm32Protocol = cm32Protocol;
	}
	
	public LoginCommand getLoginCommand(String id, String password) {
		LoginCommand command = new LoginCommand(cm32Protocol);
		command.setId(id);
		command.setPassword(password);
		return command;
	}
	
	
}

