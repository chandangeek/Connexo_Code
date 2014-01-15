/*
 * Class17LoadProfileData.java
 *
 * Created on 25 juli 2005, 10:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author koen
 */
public class Class17LoadProfileData extends AbstractClass {

    private final int DEBUG=0;

    ClassIdentification classIdentification = new ClassIdentification(17,0,false);
    private int nrOfDays;

    List dayRecords; // of type DayRecord

    /** Creates a new instance of Class17LoadProfileData */
    public Class17LoadProfileData(ClassFactory classFactory) {
        super(classFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = dayRecords.iterator();
        while(it.hasNext()) {
            DayRecord dayRecord = (DayRecord)it.next();
            strBuff.append(dayRecord.toString());
        }
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        dayRecords = new ArrayList();
//        int length = nrOfDays*classFactory.getClass14LoadProfileConfiguration().getDayRecordSize();
//        if (length != data.length)
//            throw new IOException("Class17LoadProfileData, parse(), calculated datalength for "+getNrOfDays()+" days of data ("+length+") differs from received data length ("+data.length+")");

        // corrected nr of days received...
        int correctedNrOfDays = data.length/classFactory.getClass14LoadProfileConfiguration().getDayRecordSize();

        // build dayRecords
        for (int day=0;day<correctedNrOfDays;day++) {

//        for (int day=0;day<getNrOfDays();day++) {
            DayRecord dayRecord = new DayRecord(ProtocolUtils.getSubArray2(data, day*classFactory.getClass14LoadProfileConfiguration().getDayRecordSize(), classFactory.getClass14LoadProfileConfiguration().getDayRecordSize()), classFactory);
            if (DEBUG>=1) System.out.println("KV_DEBUG> Class17LoadProfileData, parse, day="+day+", dayRecord="+dayRecord);
            dayRecords.add(dayRecord);
        }
    }



    protected ClassIdentification getClassIdentification() {
        return classIdentification;
    }

    public int getNrOfDays() {
        return nrOfDays;
    }

    public void setNrOfDays(int nrOfDays) {
        this.nrOfDays = nrOfDays;
        classIdentification.setLength(nrOfDays);
    }

    public List getDayRecords() {
        return dayRecords;
    }

}
