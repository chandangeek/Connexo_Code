/*
 * EnterTimeCommand.java
 *
 * Created on 26 juli 2006, 17:23
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.sentry.s200.core;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;

/**
 *
 * @author Koen
 */
public class EnterTimeCommand extends AbstractCommand {

    /** Creates a new instance of ForceStatusCommand */
    public EnterTimeCommand(CommandFactory cm) {
        super(cm);
    }

    // =0x"+Integer.toHexString(
    public static void main(String[] args) {
        System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new TemplateCommand(null)));
    }

    protected void parse(byte[] data) throws IOException {


    }

    protected CommandDescriptor getCommandDescriptor() {
        return new CommandDescriptor('E');
    }

    protected byte[] prepareData() throws IOException {
        Calendar cal = ProtocolUtils.getCalendar(getCommandFactory().getS200().getTimeZone());
        cal.add(Calendar.MINUTE,1);

        int iDelay = ((59 - cal.get(Calendar.SECOND))*1000)-getCommandFactory().getS200().getInfoTypeRoundtripCorrection();
        while(iDelay>0)
        {
            try
            {
                if (iDelay < 15000)
                {
                    Thread.sleep(iDelay);
                    break;
                }
                else
                {
                   Thread.sleep(15000);
                   getCommandFactory().getQueryTimeCommand();
                   iDelay -= 15000;
                }
            }
            catch(InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            }
            catch(IOException e) {
                throw new IOException("DukePower, buildFrameWriteClock, IOException, "+e.getMessage());
            }

        } // while(true)


        byte[] data = new byte[6];
        data[0] = ProtocolUtils.hex2BCD(cal.get(Calendar.MONTH)+1);
        data[1] = ProtocolUtils.hex2BCD(cal.get(Calendar.DAY_OF_MONTH));
        data[2] = ProtocolUtils.hex2BCD(cal.get(Calendar.DAY_OF_WEEK));
        data[3] = ProtocolUtils.hex2BCD(cal.get(Calendar.HOUR_OF_DAY));
        data[4] = ProtocolUtils.hex2BCD(cal.get(Calendar.MINUTE));
        data[5] = ProtocolUtils.hex2BCD(cal.get(Calendar.YEAR)%100);
        return data;
    }

}
