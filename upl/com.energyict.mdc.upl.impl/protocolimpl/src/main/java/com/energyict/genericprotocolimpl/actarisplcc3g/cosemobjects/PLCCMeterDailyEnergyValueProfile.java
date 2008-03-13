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



import com.energyict.genericprotocolimpl.common.DailyBillingEntry;
import java.io.*;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.axrdencoding.util.*;
import com.energyict.genericprotocolimpl.actarisplcc3g.*;

import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.ObjectIdentification;
import java.util.*;

/**
 *
 * @author kvds
 */
public class PLCCMeterDailyEnergyValueProfile extends AbstractPLCCObject {
    
    private Date from;
    private Date to;
    
    private Array buffer;
    
    private List dailyBillingEntries=null;
    
    /** Creates a new instance of PLCCTemplateObject */
    public PLCCMeterDailyEnergyValueProfile(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }
    
    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("1.0.98.1.0.255"), AbstractCosemObject.CLASSID_PROFILE_GENERIC);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterDailyEnergyValueProfile:\n");
        strBuff.append("   buffer="+getBuffer()+"\n");
        strBuff.append("   from="+getFrom()+"\n");
        strBuff.append("   to="+getTo()+"\n");
        return strBuff.toString();
    }

    protected void doInvoke() throws IOException {
        ProfileGeneric o = getCosemObjectFactory().getProfileGeneric(getId().getObisCode());
        Calendar calFrom,calTo;
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
        setBuffer(o.readBufferAttr(calFrom, calTo));
        
        setDailyBillingEntries(new ArrayList());
        Calendar calendar=null;
        for (int index=0;index<buffer.nrOfDataTypes();index++) {
            DailyBillingEntry dbe = new DailyBillingEntry(buffer.getDataType(index).getStructure(),getPLCCObjectFactory().getConcentrator().getTimeZone());
            
            if (dbe.getCalendar() != null)
                calendar = (Calendar)dbe.getCalendar().clone();
            else
                dbe.setCalendar((Calendar)calendar.clone());
            getDailyBillingEntries().add(dbe);
       //System.out.println(dbe+", "+getDailyBillingEntries().size());
            calendar.add(Calendar.DATE,1);
        }        
        
        
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

    private Array getBuffer() {
        return buffer;
    }

    private void setBuffer(Array buffer) {
        this.buffer = buffer;
    }

    public List getDailyBillingEntries() {
        return dailyBillingEntries;
    }

    public void setDailyBillingEntries(List dailyBillingEntries) {
        this.dailyBillingEntries = dailyBillingEntries;
    }


}
