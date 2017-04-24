/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.iskraemeco.mt83;

/**
*
* @author  jme
*/
public class MT83EventType {
	int eventCode;
	String message;
	
	public MT83EventType(String message, int eventCode) {
		this.message = message;
		this.eventCode = eventCode;
	}

	public int getEventCode() {
		return eventCode;
	}
	public String getMessage() {
		return message;
	}

	public String toString() {
		return this.message;
	}
	
}
