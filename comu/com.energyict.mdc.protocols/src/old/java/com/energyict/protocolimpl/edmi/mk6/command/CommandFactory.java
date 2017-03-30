/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CommandFactory.java
 *
 * Created on 21 maart 2006, 10:29
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.command;

import com.energyict.protocolimpl.edmi.mk6.MK6;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;


/**
 *
 * @author koen
 */
public class CommandFactory implements Serializable{

    /** Generated SerialVersionUID */
	private static final long serialVersionUID = 6867441710215466900L;

	private final int DEBUG=0;

    private MK6 mk6;

    /** Creates a new instance of CommandFactory */
    public CommandFactory(MK6 mk6) {
        this.mk6=mk6;
    }


    public String toString() {
        return "CommandFactory";
    }


    public MK6 getMk6() {
        return mk6;
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
                ic.invoke();
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

    public FileAccessInfoCommand getFileAccessInfoCommand(int registerId) throws IOException {
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> getFileAccessInfoCommand(registerId=0x"+Integer.toHexString(registerId));
		}
        FileAccessInfoCommand faic = new FileAccessInfoCommand(this);
        faic.setRegisterId(registerId);
        faic.invoke();
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> getFileAccessInfoCommand()="+faic);
		}
        return faic;
    }

    public FileAccessSearchCommand getFileAccessSearchForwardCommand(int registerId, Date date) throws IOException {
        FileAccessInfoCommand faic = getFileAccessInfoCommand(registerId);
        return getFileAccessSearchCommand(registerId, faic.getStartRecord(), date, 1);
    }

    public FileAccessSearchCommand getFileAccessSearchForwardCommand(int registerId, long startRecord, Date date) throws IOException {
        return getFileAccessSearchCommand(registerId, startRecord, date, 1);
    }

    public FileAccessSearchCommand getFileAccessSearchBackwardCommand(int registerId, long startRecord, Date date) throws IOException {
        return getFileAccessSearchCommand(registerId, startRecord, date, 0);
    }

    public FileAccessSearchCommand getFileAccessSearchCommand(int registerId, long startRecord, Date date, int direction) throws IOException {
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> getFileAccessSearchCommand(registerId=0x"+Integer.toHexString(registerId)+", startRecord="+startRecord+", date="+date+", direction="+direction+")");
		}
        FileAccessSearchCommand fasc = new FileAccessSearchCommand(this);
        fasc.setRegisterId(registerId);
        fasc.setStartRecord(startRecord);
        fasc.setDate(date);
        fasc.setDirection(direction);
        fasc.invoke();
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> getFileAccessSearchCommand()="+fasc);
		}
        return fasc;
    }

    public FileAccessReadCommand getFileAccessReadCommand(int registerId, long startRecord, int numberOfRecords, int recordOffset, int recordSize) throws IOException {
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> getFileAccessReadCommand(registerId=0x"+Integer.toHexString(registerId)+", startRecord="+startRecord+", numberOfRecords="+numberOfRecords+", recordOffset="+recordOffset+", recordSize"+recordSize+")");
		}
        FileAccessReadCommand farc = new FileAccessReadCommand(this);
        farc.setRegisterId(registerId);
        farc.setStartRecord(startRecord);
        farc.setNumberOfRecords(numberOfRecords);
        farc.setRecordOffset(recordOffset);
        farc.setRecordSize(recordSize);
        farc.invoke();
        if (DEBUG>=1) {
			System.out.println("KV_DEBUG> getFileAccessReadCommand()="+farc);
		}
        return farc;
    }

}
