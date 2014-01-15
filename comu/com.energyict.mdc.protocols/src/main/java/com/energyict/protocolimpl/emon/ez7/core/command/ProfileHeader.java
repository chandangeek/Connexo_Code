/*
 * ProfileHeader.java
 *
 * Created on 18 mei 2005, 16:19
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class ProfileHeader extends AbstractCommand {
    private static final int DEBUG=0;
    private static final String COMMAND="RPH";

    Date[] blockDate=null;
    int nrOfBlocks;
    /** Creates a new instance of ProfileHeader */
    public ProfileHeader(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ProfileHeader:\n");
        for (int dayBlockNr=0;dayBlockNr<nrOfBlocks;dayBlockNr++) {
            if (getBlockDate(dayBlockNr)!=null) {
                builder.append("dayBlockNr ").append(dayBlockNr).append(" = ").append(getBlockDate(dayBlockNr)).append("\n");
            }
        }
        return builder.toString();
    }

    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        nrOfBlocks=ez7CommandFactory.getProfileStatus().getNrOfDayBlocks();
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) throws IOException {

        blockDate = new Date[ez7CommandFactory.getProfileStatus().getNrOfDayBlocks()];

        if (DEBUG>=1) {
            System.out.println(new String(data));
        }
        CommandParser cp = new CommandParser(data);
        for (int dayBlockNr = 0; dayBlockNr < nrOfBlocks; dayBlockNr++) {
            List values = cp.getValues(ProtocolUtils.buildStringDecimal((dayBlockNr + 1), 2));
            if (values != null) {   // Generation 2 devices do not have all entries filled up - some can be left empty if not used
                int valueHHMM = Integer.parseInt((String) values.get(0));
                int valueMMDD = Integer.parseInt((String) values.get(1));
                int valueYY = Integer.parseInt((String) values.get(2));
                Calendar cal = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone());
                cal.set(Calendar.YEAR, (valueYY > 50) ? valueYY + 1900 : valueYY + 2000);
                cal.set(Calendar.MONTH, (valueMMDD / 100) - 1);
                cal.set(Calendar.DAY_OF_MONTH, (valueMMDD % 100));
                cal.set(Calendar.HOUR_OF_DAY, valueHHMM / 100);
                cal.set(Calendar.MINUTE, valueHHMM % 100);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                if ((valueYY != 0) || (valueMMDD != 0) || (valueHHMM != 0)) {
                    blockDate[dayBlockNr] = cal.getTime();
                } else {
                    blockDate[dayBlockNr] = null;
                }
            } else {
                blockDate[dayBlockNr] = null;
            }
        }

    }

    /**
     * Getter for property blockDate.
     * @return Value of property blockDate.
     */
    public java.util.Date getBlockDate(int dayBlockNr) {
        return this.blockDate[dayBlockNr];
    }



}
