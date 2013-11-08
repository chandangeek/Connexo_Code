/*
 * RegisterNumMismatchException.java
 *
 * Created on 17 mei 2006, 15:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.command;

import java.io.IOException;

/**
 * 
 * @author Koen
 */
public class CommandResponseException extends IOException {

	/** Creates a new instance of RegisterNumMismatchException */
	public CommandResponseException(String str) {
		super(str);
	}

}
