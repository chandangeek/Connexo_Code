/*
 * CommandFactory.java
 *
 * Created on 21 maart 2006, 10:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.command;

import com.energyict.protocolimpl.edmi.mk10.MK10;

import java.io.IOException;


/**
 *
 * @author koen, jme
 */
public class CommandFactory {

	private final int DEBUG=0;
	private final RegisterInfo regInfo = new RegisterInfo();

	private MK10 mk10;

	/** Creates a new instance of CommandFactory */
	public CommandFactory(MK10 mk10) {
		this.mk10=mk10;
	}

	public String toString() {
		return "CommandFactory";
	}


	public MK10 getMk10() {
		return mk10;
	}

	public void logon(String userId, String password) throws IOException {
		LogonCommand lc = new LogonCommand(this);
		lc.setLogon(userId);
		lc.setPassword(password);
		lc.invoke();
	}

	public void enterCommandLineMode() throws IOException {
		EnterCommand ec = new EnterCommand(this);
		ec.invoke();
	}

	public void exitCommandLineMode() throws IOException {
		ExitCommand ec = new ExitCommand(this);
		ec.invoke();
	}

	public InformationCommand getInformationCommand(int registerId) throws IOException {
		int retries=0;
		InformationCommand ic = new InformationCommand(this);
		ic.setRegisterId(registerId);
		while(true) {
			try {
				if (useHardCodedInfo() && isInfoHardCoded(registerId)) {
					ic.parse(this.regInfo.getInfoResponse(registerId));
				} else {
					ic.invoke();
				}
				return ic;
			}
			catch(CommandResponseException e) {
				if (retries++>=5) {
					throw new IOException("CommandFactory, getInformationCommand() Max retries "+e.toString());
				}
			}
		}

	}

	public ReadCommand getReadCommand(int registerId) throws IOException {
		int retries=0;
		ReadCommand rc = new ReadCommand(this);
		rc.setRegisterId(registerId);
		while(true) {
			try {
				rc.invoke();
				return rc;
			}
			catch(CommandResponseException e) {
				if (retries++>=5) {
					throw new IOException("CommandFactory, getInformationCommand() Max retries "+e.toString());
				}
			}
		}
	}

	public void writeCommand(int registerId, byte [] data) throws IOException {
		WriteCommand wc = new WriteCommand(this);
		wc.setData(data);
		wc.setRegisterId(registerId);
		wc.invoke();
	}

	public FileAccessReadCommand getFileAccessReadCommand(int surveyLog, int Options, long startrecord, int numberofrecords) throws IOException {
		FileAccessReadCommand farc = new FileAccessReadCommand(this);
		farc.setSurveyLog(surveyLog + 0x30);
		farc.setOptions(Options);
		farc.setStartRecord(startrecord);
		farc.setNumberOfRecords(numberofrecords);
		farc.invoke();
		if (DEBUG>=1) {
			mk10.sendDebug("KV_DEBUG> getFileAccessReadCommand()="+farc);
		}
		return farc;
	}

	public FileAccessReadCommand getFileAccessReadCommand(int surveyLog, long startrecord, int numberofrecords) throws IOException {
		FileAccessReadCommand farc = new FileAccessReadCommand(this);
		farc.setSurveyLog(surveyLog + 0x30);
		farc.setOptions(0x00);
		farc.setStartRecord(startrecord);
		farc.setNumberOfRecords(numberofrecords);
		farc.invoke();
		if (DEBUG>=1) {
			mk10.sendDebug("KV_DEBUG> getFileAccessReadCommand()="+farc);
		}
		return farc;
	}

	private boolean isInfoHardCoded(int registerId) {
		return this.regInfo.isInfoHardCoded(registerId);
	}

	private boolean useHardCodedInfo() {
		return getMk10().useHardCodedInfo();
	}

}
