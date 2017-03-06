package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.core.DateTimeBuilder;

import java.util.Date;

/**
 *
 * @author koen
 */
public class TimeInfo {
    
    //private final int CLOCK_COMMAND_READ_DST = 0xF061; // dst corrected time
    private final int CLOCK_COMMAND_READ_STD = 0xF03D; // std time
    private final int CLOCK_COMMAND_WRITE_STD = 0xF03D; // std time
    private final int DST_USED = 0xF015;
    CommandLineProtocol commandLineProtocol;
    
    /** Creates a new instance of TimeInfo */
    public TimeInfo(CommandLineProtocol commandLineProtocol) {
        this.commandLineProtocol = commandLineProtocol;
    }
    
// Not used for the moment. All reported times are standard time!
//    public void verifyTimeZone() throws IOException {
//        if (mk6.getCommandFactory().getReadCommand(DST_USED).getRegister().getBigDecimal().intValue()==0) {
//            return ProtocolUtils.getWinterTimeZone(mk6.getTimeZone());
//        }
//        else
//            return mk6.getTimeZone();
//    }
    
    public void setTime(Date timeToSet) {
        byte[] data = DateTimeBuilder.getDDMMYYHHMMSSDataFromDate(timeToSet, commandLineProtocol.getTimeZone());
        commandLineProtocol.getCommandFactory().writeCommand(CLOCK_COMMAND_WRITE_STD, data);
    }
    
    public Date getTime() {
        return commandLineProtocol.getCommandFactory().getReadCommand(CLOCK_COMMAND_READ_STD).getRegister().getDate();
    }
}
