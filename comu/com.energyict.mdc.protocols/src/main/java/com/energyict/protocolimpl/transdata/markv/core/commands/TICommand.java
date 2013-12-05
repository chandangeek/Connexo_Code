/*
 * TICommand.java
 *
 * Created on 11 augustus 2005, 11:40
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;

import com.energyict.protocol.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author koen
 */
public class TICommand  extends AbstractCommand {

    private static final CommandIdentification commandIdentification = new CommandIdentification("TI");

    Date date;

    /** Creates a new instance of TICommand */
    public TICommand(CommandFactory commandFactory) {
        super(commandFactory);
    }

    protected void prepareBuild() {
        Calendar cal = ProtocolUtils.getCalendar(getCommandFactory().getMarkV().getTimeZone());
        cal.add(Calendar.MILLISECOND,getCommandFactory().getMarkV().getInfoTypeRoundtripCorrection());

        // recalculate roundtrip using the delay from terminal mode...
//        int extraDelay = getCommandFactory().getMarkV().getInfoTypeForcedDelay()*(16+14+2);
//        cal.add(Calendar.MILLISECOND,extraDelay);

        String[] arguments = new String[7];
        arguments[0] = ProtocolUtils.buildStringDecimal(cal.get(Calendar.SECOND),2);
        arguments[1] = ProtocolUtils.buildStringDecimal(cal.get(Calendar.MINUTE),2);
        arguments[2] = ProtocolUtils.buildStringDecimal(cal.get(Calendar.HOUR_OF_DAY),2);
        arguments[3] = ProtocolUtils.buildStringDecimal(cal.get(Calendar.DAY_OF_MONTH),2);
        arguments[4] = ProtocolUtils.buildStringDecimal(cal.get(Calendar.MONTH)+1,2);
        arguments[5] = ProtocolUtils.buildStringDecimal(cal.get(Calendar.YEAR)-2000,2);
        arguments[6] = ProtocolUtils.buildStringDecimal(cal.get(Calendar.DAY_OF_WEEK),2);
        //getCommandIdentification().setArguments(arguments);

        getCommandIdentification().setCommand("TI\r\n"+arguments[0]+"\r\n"+arguments[1]+"\r\n"+arguments[2]+"\r\n"+arguments[3]+"\r\n"+arguments[4]+"\r\n"+arguments[5]+"\r\n"+arguments[6]);

    }

    protected void parse(String strData) throws IOException {
    }

    protected CommandIdentification getCommandIdentification() {
        return commandIdentification;
    }

}
