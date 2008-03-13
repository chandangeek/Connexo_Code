/*
 * ProfileHeader.java
 *
 * Created on 18 mei 2005, 16:19
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import java.io.*;
import java.util.*;
import java.text.*;

import com.energyict.cbo.NestedIOException;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.emon.ez7.core.*;
import com.energyict.dialer.connection.ConnectionException;

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
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ProfileHeader:\n");
        for (int dayBlockNr=0;dayBlockNr<nrOfBlocks;dayBlockNr++) {
            if (getBlockDate(dayBlockNr)!=null)
               strBuff.append("dayBlockNr "+dayBlockNr+" = "+getBlockDate(dayBlockNr)+"\n");  
        }
        return strBuff.toString();
    }
    
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        nrOfBlocks=ez7CommandFactory.getProfileStatus().getNrOfDayBlocks();
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    private void parse(byte[] data) throws ConnectionException, IOException {
        
        blockDate = new Date[ez7CommandFactory.getProfileStatus().getNrOfDayBlocks()];
        
        if (DEBUG>=1) 
           System.out.println(new String(data)); 
        CommandParser cp = new CommandParser(data); 
        for (int dayBlockNr=0;dayBlockNr<nrOfBlocks;dayBlockNr++) {
            List values = cp.getValues(ProtocolUtils.buildStringDecimal((dayBlockNr+1), 2));
            int valueHHMM = Integer.parseInt((String)values.get(0));
            int valueMMDD = Integer.parseInt((String)values.get(1));
            int valueYY = Integer.parseInt((String)values.get(2));
            Calendar cal = ProtocolUtils.getCalendar(ez7CommandFactory.getEz7().getTimeZone());
            cal.set(Calendar.YEAR,(valueYY>50)?valueYY+1900:valueYY+2000);
            cal.set(Calendar.MONTH,(valueMMDD/100)-1);
            cal.set(Calendar.DAY_OF_MONTH,(valueMMDD%100));
            cal.set(Calendar.HOUR_OF_DAY,valueHHMM/100);
            cal.set(Calendar.MINUTE,valueHHMM%100);
            cal.set(Calendar.SECOND,0);
            cal.set(Calendar.MILLISECOND,0);
            if ((valueYY!=0) || (valueMMDD!=0) || (valueHHMM!=0))
                blockDate[dayBlockNr] = cal.getTime();
            else
                blockDate[dayBlockNr] = null;
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
