/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * AccessPartiel.java
 *
 * Created on 21 februari 2007, 13:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran2p.core;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;
import com.energyict.protocolimpl.edf.trimarandlms.common.DateType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class AccessPartiel extends AbstractTrimaranObject {
    
    private Date dateAccess;
    private Calendar calendarAccess;
    private int nomAccess;
    
    
    /** Creates a new instance of AccessPartiel */
    public AccessPartiel(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("*** AccessPartiel: ***\n");
        strBuff.append("	- calendarAccess="+getCalendarAccess()+"\n");
        strBuff.append("	- dateAccess="+getDateAccess()+"\n");
        strBuff.append("	- nomAccess="+getNomAccess()+"\n");
        return strBuff.toString();
    }      

    protected int getVariableName() {
        return 8;
    }
    
    protected byte[] prepareBuild() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(0x01); // 1 element of sequence
        baos.write(0x02); // structure
        baos.write(0x02); // 2 elements in the structure
        baos.write(0x0F); // integer8
        baos.write((byte)getNomAccess());
        baos.write(0x04); // bit string
        baos.write(0x28); // 40 bits following
        if (getDateAccess()!=null) {
           DateType dt = new DateType(getDateAccess(), getTrimaranObjectFactory().getTrimaran().getTimeZone()); 
           baos.write(dt.getData());  
        }
        else {
           baos.write(new byte[]{(byte)0xB8,0x21,0x00,0x06,0x32});  // hardcoded 1/1/1992 00:00 (see trimaran+ doc page 34)
        }
        return baos.toByteArray();
    }
    
    protected void parse(byte[] data) throws IOException {
        TrimaranDataContainer dc = new TrimaranDataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
        setNomAccess(dc.getRoot().getInteger(0));
        DateType dt = new DateType(dc.getRoot().getLong(1), getTrimaranObjectFactory().getTrimaran().getTimeZone());
        setDateAccess(dt.getCalendar().getTime());
        setCalendarAccess(dt.getCalendar());
    }    
    
    public Date getDateAccess() {
        return dateAccess;
    }

    public void setDateAccess(Date dateAccess) {
        this.dateAccess = dateAccess;
    }

    public int getNomAccess() {
        return nomAccess;
    }

    public void setNomAccess(int nomAccess) {
        this.nomAccess = nomAccess;
    }

    public Calendar getCalendarAccess() {
        return calendarAccess;
    }

    public void setCalendarAccess(Calendar calendarAccess) {
        this.calendarAccess = calendarAccess;
    }
}
