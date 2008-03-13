/*
 * PLCCTemplateObject.java
 *
 * Created on 3 december 2007, 13:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;



import com.energyict.genericprotocolimpl.common.*;
import java.io.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.*;

import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.ObjectIdentification;
import java.util.*;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.ProfileGeneric;

/**
 *
 * @author kvds
 */
public class PLCCMeterLoadProfileEnergy extends AbstractPLCCObject {
    
    private Array buffer=null;
    private Date from;
    private Date to;    
    private long capturePeriod=-1;
    private List loadProfileEntries=null;
    ProfileGeneric profileGeneric=null;
    Calendar calFrom,calTo;
    private boolean compressed=false;
    
    /** Creates a new instance of PLCCTemplateObject */
    public PLCCMeterLoadProfileEnergy(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        if (isCompressed())
            return new ObjectIdentification(ObisCode.fromString("1.1.99.1.0.255"), AbstractCosemObject.CLASSID_PROFILE_GENERIC);
        else
            return new ObjectIdentification(ObisCode.fromString("1.0.99.1.0.255"), AbstractCosemObject.CLASSID_PROFILE_GENERIC);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterLoadProfileEnergy:\n");
        
        try {
            strBuff.append("   capturePeriod="+getCapturePeriod()+" s\n");
        }
        catch(IOException e) {
            strBuff.append("   capturePeriod="+e.toString()+"\n");
        }
        try {
            strBuff.append("   buffer="+getBuffer()+"\n");
        }
        catch(IOException e) {
            strBuff.append("   buffer="+e.toString()+"\n");
        }
        
        strBuff.append("   from="+getFrom()+"\n");
        strBuff.append("   to="+getTo()+"\n");
        for (int index=0;index<buffer.nrOfDataTypes();index++) {
            LoadProfileEntry l = (LoadProfileEntry)loadProfileEntries.get(index);
            strBuff.append("   LoadProfileEntry "+index+"="+l.getCalendar().getTime()+", "+l+"\n");
        }        
        return strBuff.toString();
    }  

    protected void doInvoke() throws IOException {
        profileGeneric = getCosemObjectFactory().getProfileGeneric(getId().getObisCode());
        
        if (getTo()==null)
            calTo = ProtocolUtils.getCalendar(getPLCCObjectFactory().getConcentrator().getTimeZone()); // KV_TO_DO concentrator timezone if all meters are in the same timezone...
        else {
            calTo = ProtocolUtils.getCalendar(getPLCCObjectFactory().getConcentrator().getTimeZone());
            calTo.setTime(getTo());
        }
        
        if (getFrom()==null) {
            calFrom = ProtocolUtils.getCalendar(getPLCCObjectFactory().getConcentrator().getTimeZone()); // KV_TO_DO concentrator timezone if all meters are in the same timezone...
            calFrom.add(Calendar.DATE,-1);
        }
        else {
            calFrom = ProtocolUtils.getCalendar(getPLCCObjectFactory().getConcentrator().getTimeZone());
            calFrom.setTime(getFrom());
        }
    }
    
    public List toIntervalDatas() throws IOException {
        // build profileData
        List intervalDatas = new ArrayList();
        int profileInterval = (int)getCapturePeriod();
        Calendar calendar = null;
        Iterator it = getLoadProfileEntries().iterator();
        while(it.hasNext()) {
            LoadProfileEntry loadProfileEntry = (LoadProfileEntry)it.next();
            if (loadProfileEntry.getCalendar() != null) {
                calendar = loadProfileEntry.getCalendar();
                
                // do not store the non aligned intervals. Since the meters report cumulative values, we can remove the value
                if (((calendar.getTime().getTime()/1000)%profileInterval) != 0) {
                    //System.out.println("**************************** SKIP value due to non aligned datetime... ("+calendar.getTime()+")");
                    continue;
                }
                else {
                    //System.out.println("**************************** new datetime... ("+calendar.getTime()+")");
                }
            }
            IntervalData intervalData = new IntervalData(new Date(calendar.getTime().getTime()),StatusCodeProfile.intervalStateBits(loadProfileEntry.getStatus()),loadProfileEntry.getStatus());
            intervalData.addValue(loadProfileEntry.getValue());
            intervalDatas.add(intervalData);
            calendar.add(Calendar.SECOND, profileInterval);
        }
        return intervalDatas;
    }
    
    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public Date getTo() {
        return to;
    }

    public void setTo(Date to) {
        this.to = to;
    }

    public Array getBuffer() throws IOException {
        if (buffer == null) {
            buffer = profileGeneric.readBufferAttr(calFrom, calTo);
        }
        return buffer;
    }

    public long getCapturePeriod() throws IOException {
        if (capturePeriod == -1)
             capturePeriod = profileGeneric.readCapturePeriodAttr().intValue();        
        return capturePeriod;
    }

    public void writeCapturePeriod(long capturePeriod) throws IOException {
        profileGeneric.writeCapturePeriodAttr(new Unsigned32(capturePeriod));
        this.capturePeriod = capturePeriod;
    }

    public List getLoadProfileEntries() throws IOException {
        if (loadProfileEntries == null) {
            if (isCompressed()) {
                byte[] data = profileGeneric.getData(2,calFrom, calTo);
                LoadProfileDecompressor o = new LoadProfileDecompressor(data, getPLCCObjectFactory().getConcentrator().getTimeZone());
                o.deCompress();
                loadProfileEntries = o.getLoadProfileEntries();
            }
            else {
                loadProfileEntries = new ArrayList();
                for (int index=0;index<getBuffer().nrOfDataTypes();index++) {
                    LoadProfileEntry loadprofileEntry = new LoadProfileEntry(getBuffer().getDataType(index).getStructure(),getPLCCObjectFactory().getConcentrator().getTimeZone());
                    loadProfileEntries.add((loadprofileEntry));
                    //System.out.println(loadprofileEntry);
                }     
            }
        }
        return loadProfileEntries;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }
}
