package com.energyict.protocolimpl.instromet.connection;

import com.energyict.protocolimpl.instromet.core.InstrometProtocol;

public class LogoffCommand extends AbstractCommand {
	
	public LogoffCommand(InstrometProtocol instrometProtocol) {
		super(instrometProtocol);
	}
	
	protected Command preparebuild() {
		return new Command('L');
	}

}
