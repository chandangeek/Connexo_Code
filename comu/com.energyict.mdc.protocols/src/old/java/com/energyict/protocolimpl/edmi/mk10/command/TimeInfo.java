/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TimeInfo.java
 *
 * Created on 27 maart 2006, 14:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.command;

import com.energyict.protocolimpl.edmi.mk10.MK10;
import com.energyict.protocolimpl.edmi.mk10.core.DateTimeBuilder;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author koen
 */
public class TimeInfo {

	private final int CLOCK_COMMAND_READ_STD = 0xF03D; // std time
	private final int CLOCK_COMMAND_WRITE_STD = 0xF03D; // std time
	private final int DST_USED = 0xF015;
	MK10 mk10;

	/** Creates a new instance of TimeInfo */
	public TimeInfo(MK10 mk10) {
		this.mk10=mk10;
	}

	public void setTime() throws IOException {
		byte[] data = DateTimeBuilder.getDDMMYYHHMMSSDataFromDate(new Date(),mk10.getTimeZone());
		mk10.getCommandFactory().writeCommand(CLOCK_COMMAND_WRITE_STD, data);
	}

	public Date getTime() throws IOException {
		return mk10.getCommandFactory().getReadCommand(CLOCK_COMMAND_READ_STD).getRegister().getDate();
	}

}
