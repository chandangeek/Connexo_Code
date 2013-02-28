/*
 * ProfileStatus.java
 *
 * Created on 13 mei 2005, 13:55
 */

package com.energyict.protocolimpl.emon.ez7.core.command;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.emon.ez7.core.EZ7CommandFactory;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 *
 * @author  Koen
 */
public class ProfileStatus extends AbstractCommand {
    private static final int DEBUG=0;
    private static final String COMMAND="RP";
    
    int profileInterval; // 300 or 900 sec
    boolean profileCollectionStatus=false;
    int profileResolution; // nr of bytes, 1 or 2
    
    int currentDayBlock;
    int nrOfDayBlocks;
    Date firstBlockStart=null;
    Date currentBlockStart=null;
    
    /** Creates a new instance of ProfileStatus */
    public ProfileStatus(EZ7CommandFactory ez7CommandFactory) {
        super(ez7CommandFactory);
    }

    public String toString() {
        return "ProfileStatus: profileInterval="+profileInterval+
               ", profileCollectionStatus="+profileCollectionStatus+
               ", profileResolution="+profileResolution+
               ", currentDayBlock="+currentDayBlock+
               ", nrOfDayBlocks="+nrOfDayBlocks+
               ", firstBlockStart="+firstBlockStart+
               ", currentBlockStart="+currentBlockStart;
    }
    
    public void build() throws ConnectionException, IOException {
        // retrieve profileStatus
        byte[] data = ez7CommandFactory.getEz7().getEz7Connection().sendCommand(COMMAND);
        parse(data);
    }

    public void parse(byte[] data) {
        
        List values=null;
        int baseVal;
        Calendar cal;

        if (DEBUG>=1) 
           System.out.println(new String(data)); 
        CommandParser cp = new CommandParser(data); 
        
        values =cp.getValues("LINE-1:");
        baseVal = Integer.parseInt((String)values.get(7),16);
        
        // parse profileInterval
        int val = (baseVal - (baseVal/0x1000)*0x1000)/0x100;
        if (val == 1) setProfileInterval(5*60);
        if (val == 2) setProfileInterval(15*60);
        
        // parse profileCollectionStatus
        val = (baseVal/0x1000);
        
        setProfileCollectionStatus(((val&0x1)==0x1));
        
        // parse profileResolution
        setProfileResolution(((val&0x4)>>2)+1);

        // parse currentBlockStart;
        if (ez7CommandFactory==null)
            cal = ProtocolUtils.getCleanCalendar(TimeZone.getDefault());
        else
            cal = ProtocolUtils.getCleanCalendar(ez7CommandFactory.getEz7().getTimeZone());
        
        baseVal = Integer.parseInt((String)values.get(4),10); // 00YY
        cal.set(Calendar.YEAR,(baseVal>50)?baseVal+1900:baseVal+2000);
        baseVal = Integer.parseInt((String)values.get(5),10); // HHMM
        cal.set(Calendar.HOUR_OF_DAY,baseVal/100);
        cal.set(Calendar.MINUTE,baseVal%100);
        baseVal = Integer.parseInt((String)values.get(3),10); // MMDD
        cal.set(Calendar.MONTH,(baseVal/100)-1);
        cal.set(Calendar.DAY_OF_MONTH,(baseVal%100));
        if (baseVal!=0)
            // 28/02/2013 - Email from Kantol "The 1st line of data in the block represents usage from 23:45 to 00:00; so the timestamp for the 1st line is 00:00 which represents the end-of-interval."
            // Calendar represents current block end-of-interval timestamp, so we should subtract the profileInterval to get the start-of-interval timestamp
            cal.add(Calendar.SECOND,- getProfileInterval());
            setCurrentBlockStart(cal.getTime());
        
        values =cp.getValues("LINE-2:");
        baseVal = Integer.parseInt((String)values.get(0),16);
        
        // parse currentDayBlock;
        setCurrentDayBlock(baseVal/0x100);
        
        // parse nrOfDayBlocks;
        setNrOfDayBlocks(baseVal%0x100);

        // parse firstBlockStart;
        if (ez7CommandFactory==null)
            cal = ProtocolUtils.getCleanCalendar(TimeZone.getDefault());
        else
            cal = ProtocolUtils.getCleanCalendar(ez7CommandFactory.getEz7().getTimeZone());
        baseVal = Integer.parseInt((String)values.get(4),10); // 00YY
        cal.set(Calendar.YEAR,(baseVal>50)?baseVal+1900:baseVal+2000);
        baseVal = Integer.parseInt((String)values.get(5),10); // HHMM
        cal.set(Calendar.HOUR_OF_DAY,baseVal/100);
        cal.set(Calendar.MINUTE,baseVal%100);
        baseVal = Integer.parseInt((String)values.get(3),10); // MMDD
        cal.set(Calendar.MONTH,(baseVal/100)-1);
        cal.set(Calendar.DAY_OF_MONTH,(baseVal%100));
        if (baseVal!=0)
            setFirstBlockStart(cal.getTime());
        
    }
    
    static public void main(String[] args) {
        byte[] data = {0x0D,0x0A,0x4C,0x49,0x4E,0x45,0x2D,0x31,0x3A,0x09,0x30,0x35,0x31,0x33,0x09,0x30,0x30,0x30,0x35,0x09,0x31,0x34,0x31,0x38,0x09,0x30,0x35,0x31,0x33,0x09,0x30,0x30,0x30,0x35,0x09,0x30,0x30,0x30,0x30,0x09,0x32,0x34,0x30,0x30,0x09,0x35,0x32,0x30,0x30,0x0D,0x0A,0x4C,0x49,0x4E,0x45,0x2D,0x32,0x3A,0x09,0x30,0x34,0x32,0x34,0x09,0x32,0x33,0x30,0x38,0x09,0x30,0x30,0x30,0x30,0x09,0x30,0x35,0x31,0x30,0x09,0x30,0x30,0x30,0x35,0x09,0x30,0x30,0x30,0x30,0x09,0x30,0x30,0x30,0x30,0x09,0x30,0x38,0x46,0x46,0x0D,0x0A};
        ProfileStatus ps = new ProfileStatus(null);
        ps.parse(data);
        System.out.println(ps);
    }
    
    /**
     * Getter for property profileInterval.
     * @return Value of property profileInterval.
     */
    public int getProfileInterval() {
        return profileInterval;
    }
    
    /**
     * Setter for property profileInterval.
     * @param profileInterval New value of property profileInterval.
     */
    public void setProfileInterval(int profileInterval) {
        this.profileInterval = profileInterval;
    }
    
    /**
     * Getter for property profileCollectionStatus.
     * @return Value of property profileCollectionStatus.
     */
    public boolean isProfileCollectionStatus() {
        return profileCollectionStatus;
    }
    
    /**
     * Setter for property profileCollectionStatus.
     * @param profileCollectionStatus New value of property profileCollectionStatus.
     */
    public void setProfileCollectionStatus(boolean profileCollectionStatus) {
        this.profileCollectionStatus = profileCollectionStatus;
    }
    
    /**
     * Getter for property profileResolution.
     * @return Value of property profileResolution.
     */
    public int getProfileResolution() {
        return profileResolution;
    }    

    /**
     * Setter for property profileResolution.
     * @param profileResolution New value of property profileResolution.
     */
    public void setProfileResolution(int profileResolution) {
        this.profileResolution = profileResolution;
    }
    
    /**
     * Getter for property currentDayBlock.
     * @return Value of property currentDayBlock.
     */
    public int getCurrentDayBlock() {
        return currentDayBlock;
    }
    
    /**
     * Setter for property currentDayBlock.
     * @param currentDayBlock New value of property currentDayBlock.
     */
    public void setCurrentDayBlock(int currentDayBlock) {
        this.currentDayBlock = currentDayBlock;
    }
    
    /**
     * Getter for property nrOfDayBlocks.
     * @return Value of property nrOfDayBlocks.
     */
    public int getNrOfDayBlocks() {
        return nrOfDayBlocks;
    }
    
    /**
     * Setter for property nrOfDayBlocks.
     * @param nrOfDayBlocks New value of property nrOfDayBlocks.
     */
    public void setNrOfDayBlocks(int nrOfDayBlocks) {
        this.nrOfDayBlocks = nrOfDayBlocks;
    }
    
    /**
     * Getter for property firstBlockStart.
     * @return Value of property firstBlockStart.
     */
    public java.util.Date getFirstBlockStart() {
        return firstBlockStart;
    }
    
    /**
     * Setter for property firstBlockStart.
     * @param firstBlockStart New value of property firstBlockStart.
     */
    public void setFirstBlockStart(java.util.Date firstBlockStart) {
        this.firstBlockStart = firstBlockStart;
    }
    
    /**
     * Getter for property currentBlockStart.
     * @return Value of property currentBlockStart.
     */
    public java.util.Date getCurrentBlockStart() {
        return currentBlockStart;
    }
    
    /**
     * Setter for property currentBlockStart.
     * @param currentBlockStart New value of property currentBlockStart.
     */
    public void setCurrentBlockStart(java.util.Date currentBlockStart) {
        this.currentBlockStart = currentBlockStart;
    }
    
}
