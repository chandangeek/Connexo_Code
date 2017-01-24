/*
 * LoadProfileDataBlock.java
 *
 * Created on 29 oktober 2004, 9:48
 */

package com.energyict.protocolimpl.iec1107.enermete70x;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class LoadProfileDataBlock {

    int profileInterval;
    Date firstStartingDate;
    List loadProfileEntries; // of type LoadProfileEntry

    /** Creates a new instance of LoadProfileDataBlock */
    public LoadProfileDataBlock(Date firstStartingDate, List loadProfileEntries, int profileInterval) {
        this.firstStartingDate=firstStartingDate;
        this.loadProfileEntries=loadProfileEntries;
        this.profileInterval=profileInterval;
    }

    /**
     * Getter for property firstStartingDate.
     * @return Value of property firstStartingDate.
     */
    public java.util.Date getFirstStartingDate() {
        return firstStartingDate;
    }

    public java.util.Date getFirstIntervalEndDate() {
        return new Date(firstStartingDate.getTime()+getProfileInterval()*3600000L);
    }

    /**
     * Getter for property loadProfileEntries.
     * @return Value of property loadProfileEntries.
     */
    public java.util.List getLoadProfileEntries() {
        return loadProfileEntries;
    }

    public String toString() {
        int count=0;
        long firstIntervalEndTimeStamp = firstStartingDate.getTime() + (getProfileInterval()*1000);
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("interval = "+getProfileInterval()+" sec\n");
        strBuff.append(getFirstStartingDate()+"\n");
        Iterator it = getLoadProfileEntries().iterator();
        while(it.hasNext()) {
            LoadProfileEntry lpe = (LoadProfileEntry)it.next();
            strBuff.append("interval "+count+", "+(new Date(firstIntervalEndTimeStamp+count*(getProfileInterval()*1000)))+", "+lpe.toString()+"\n");
            count++;
        }
        return strBuff.toString();
    }



    /**
     * Getter for property profileInterval.
     * @return Value of property profileInterval.
     */
    public int getProfileInterval() {
        return profileInterval;
    }

    //public int

}
