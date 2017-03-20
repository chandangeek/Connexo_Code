package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.core.DataType;
import com.energyict.protocolimpl.edmi.common.core.DateTimeBuilder;

import java.util.Date;

/**
 *
 * @author koen
 */
public class TimeInfo {

    private final int CLOCK_COMMAND_READ_STD = 0xF03D; // std time
    private final int CLOCK_COMMAND_WRITE_STD = 0xF03D; // std time

    CommandLineProtocol commandLineProtocol;
    
    /** Creates a new instance of TimeInfo */
    public TimeInfo(CommandLineProtocol commandLineProtocol) {
        this.commandLineProtocol = commandLineProtocol;
    }

    public void setTime(Date timeToSet) {
        byte[] data = DateTimeBuilder.getDDMMYYHHMMSSDataFromDate(timeToSet, ProtocolUtils.getWinterTimeZone(commandLineProtocol.getTimeZone()));
        commandLineProtocol.getCommandFactory().writeCommand(CLOCK_COMMAND_WRITE_STD, data);
    }
    
    public Date getTime() {
        return commandLineProtocol.getCommandFactory().getReadCommand(CLOCK_COMMAND_READ_STD, DataType.T_TIME_DATE_SINCE__1_97).getRegister().getDate();
    }
}
