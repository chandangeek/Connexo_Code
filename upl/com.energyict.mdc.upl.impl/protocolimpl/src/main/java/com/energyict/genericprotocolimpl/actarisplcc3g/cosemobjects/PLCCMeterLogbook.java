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



import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ObjectIdentification;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.genericprotocolimpl.common.LogbookEntry;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolUtils;

/**
 *
 * @author kvds
 */
public class PLCCMeterLogbook extends AbstractPLCCObject {

    private Array buffer;
    private Date from;
    private Date to;

    private List meterEvents;

    /** Creates a new instance of PLCCTemplateObject */
    public PLCCMeterLogbook(PLCCObjectFactory objectFactory) {
        super(objectFactory);
    }

    protected ObjectIdentification getId() {
        return new ObjectIdentification(ObisCode.fromString("1.0.99.98.1.255"), DLMSClassId.PROFILE_GENERIC.getClassId());
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PLCCMeterLogbook:\n");
        strBuff.append("   from="+getFrom()+"\n");
        strBuff.append("   to="+getTo()+"\n");
        for (int index=0;index<buffer.nrOfDataTypes();index++) {
            MeterEvent m = (MeterEvent)getMeterEvents().get(index);
            strBuff.append("   meterEvent "+index+"="+m.getTime()+", "+m+"\n");
        }
        return strBuff.toString();
    }

    protected void doInvoke() throws IOException {
        ProfileGeneric o = getCosemObjectFactory().getProfileGeneric(getId().getObisCode());
        Calendar calFrom,calTo;
        if (getTo()==null) {
			calTo = ProtocolUtils.getCalendar(getPLCCObjectFactory().getConcentrator().getTimeZone()); // KV_TO_DO concentrator timezone if all meters are in the same timezone...
		} else {
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

        setMeterEvents(new ArrayList());
        for (int index=0;index<buffer.nrOfDataTypes();index++) {
            LogbookEntry logbookEntry = new LogbookEntry(buffer.getDataType(index).getStructure(),getPLCCObjectFactory().getConcentrator().getTimeZone());
            if (logbookEntry.getDate() != null) {
                if (!logbookEntry.getDate().after(new Date())) {
					getMeterEvents().add((logbookEntry).toMeterEvent());
				}
            }
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

    public List getMeterEvents() {
        return meterEvents;
    }

    private void setMeterEvents(List meterEvents) {
        this.meterEvents = meterEvents;
    }


}
