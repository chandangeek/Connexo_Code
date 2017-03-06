package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.connection.MiniECommandLineConnection;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;


/**
 * @author koen
 */
public class CommandFactory implements Serializable {

    private final RegisterInfo regInfo = new RegisterInfo();

    private CommandLineProtocol protocol;

    /**
     * Creates a new instance of CommandFactory
     */
    public CommandFactory(CommandLineProtocol protocol) {
        this.protocol = protocol;
    }


    public String toString() {
        return "CommandFactory";
    }


    public CommandLineProtocol getProtocol() {
        return protocol;
    }

    public void logon(String userId, String password) {
        LogonCommand lc = new LogonCommand(this);
        lc.setLogon(userId);
        lc.setPassword(password);
        lc.invoke();
    }

    public void enterCommandLineMode() {
        if (! (getProtocol().getCommandLineConnection() instanceof MiniECommandLineConnection)) {
            EnterCommand ec = new EnterCommand(this);
            ec.invoke();
        } // The Enter command is not supported when using Mini E connection
    }

    public void exitCommandLineMode() {
        ExitCommand ec = new ExitCommand(this);
        ec.invoke();
    }

    public InformationCommand getInformationCommand(int registerId) {
        InformationCommand ic = new InformationCommand(this);
        ic.setRegisterId(registerId);
        if (useHardCodedInfo() && isInfoHardCoded(registerId)) {
            try {
                ic.parse(this.regInfo.getInfoResponse(registerId));
            } catch (IOException e) {
                throw ConnectionCommunicationException.unexpectedIOException(e); // As we are parsing hard-coded data, this should in fact never fail, so it is expected we shouldn't reach this point
            }
        } else {
            ic.invoke();
        }
        return ic;
    }

    public ReadCommand getReadCommand(int registerId) {
        ReadCommand rc = new ReadCommand(this);
        rc.setRegisterId(registerId);
        rc.invoke();
        return rc;
    }

    public void writeCommand(int registerId, byte[] data) {
        WriteCommand wc = new WriteCommand(this);
        wc.setData(data);
        wc.setRegisterId(registerId);
        wc.invoke();
    }

    public GeniusFileAccessReadCommand getGeniusFileAccessReadCommand(int registerId, long startRecord, int numberOfRecords, int recordOffset, int recordSize) {
        GeniusFileAccessReadCommand farc = new GeniusFileAccessReadCommand(this);
        farc.setRegisterId(registerId);
        farc.setStartRecord(startRecord);
        farc.setNumberOfRecords(numberOfRecords);
        farc.setRecordOffset(recordOffset);
        farc.setRecordSize(recordSize);
        farc.invoke();
        return farc;
    }

    public GeniusFileAccessInfoCommand getGeniesFileAccessInfoCommand(int registerId) {
        GeniusFileAccessInfoCommand faic = new GeniusFileAccessInfoCommand(this);
        faic.setRegisterId(registerId);
        faic.invoke();
        return faic;
    }

    public GeniusFileAccessSearchCommand getGeniusFileAccessSearchForwardCommand(int registerId, Date date) {
        GeniusFileAccessInfoCommand faic = getGeniesFileAccessInfoCommand(registerId);
        return getGeniusFileAccessSearchCommand(registerId, faic.getStartRecord(), date, 1);
    }

    public GeniusFileAccessSearchCommand getGeniusFileAccessSearchForwardCommand(int registerId, long startRecord, Date date) {
        return getGeniusFileAccessSearchCommand(registerId, startRecord, date, 1);
    }

    public GeniusFileAccessSearchCommand getGeniusFileAccessSearchBackwardCommand(int registerId, long startRecord, Date date) {
        return getGeniusFileAccessSearchCommand(registerId, startRecord, date, 0);
    }

    public GeniusFileAccessSearchCommand getGeniusFileAccessSearchCommand(int registerId, long startRecord, Date date, int direction) {
        GeniusFileAccessSearchCommand fasc = new GeniusFileAccessSearchCommand(this);
        fasc.setRegisterId(registerId);
        fasc.setStartRecord(startRecord);
        fasc.setDate(date);
        fasc.setDirection(direction);
        fasc.invoke();
        return fasc;
    }


    public Atlas1FileAccessReadCommand getAtlas1FileAccessReadCommand(int surveyLog, int Options, long startRecord, int numberOfRecords) {
        Atlas1FileAccessReadCommand farc = new Atlas1FileAccessReadCommand(this);
        farc.setSurveyLog(surveyLog);
        farc.setOptions(Options);
        farc.setStartRecord(startRecord);
        farc.setNumberOfRecords(numberOfRecords);
        farc.invoke();
        return farc;
    }

    public Atlas1FileAccessReadCommand getAtlas1FileAccessReadCommand(int surveyLog, long startRecord, int numberOfRecords) {
        Atlas1FileAccessReadCommand farc = new Atlas1FileAccessReadCommand(this);
        farc.setSurveyLog(surveyLog);
        farc.setOptions(0x00);
        farc.setStartRecord(startRecord);
        farc.setNumberOfRecords(numberOfRecords);
        farc.invoke();
        return farc;
    }

    private boolean useHardCodedInfo() {
        return getProtocol().useHardCodedInfo();
    }

    private boolean isInfoHardCoded(int registerId) {
        return this.regInfo.isInfoHardCoded(registerId);
    }
}