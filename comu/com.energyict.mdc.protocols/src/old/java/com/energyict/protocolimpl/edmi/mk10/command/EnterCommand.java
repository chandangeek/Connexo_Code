/*
 * EnterCommand.java
 *
 * Created on 21 maart 2006, 10:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.command;

/**
 *
 * @author koen
 */
public class EnterCommand extends AbstractCommand {

	/** Creates a new instance of EnterCommand */
	public EnterCommand(CommandFactory commandFactory) {
		super(commandFactory);
	}

	protected byte[] prepareBuild() {
		return null;
	}

	protected void parse(byte[] data) {

	}

}
