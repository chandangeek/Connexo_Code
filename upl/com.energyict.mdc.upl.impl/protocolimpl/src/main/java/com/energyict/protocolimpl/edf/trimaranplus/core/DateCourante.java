/*
 * AccessPartiel.java
 *
 * Created on 21 februari 2007, 13:14
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.protocolimpl.edf.trimaranplus.core.axdr.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public class DateCourante extends AbstractTrimaranObject {
    
    private Date date;
    
    /** Creates a new instance of AccessPartiel */
    public DateCourante(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DateCourante:\n");
        strBuff.append("   date="+getDate()+"\n");
        return strBuff.toString();
    }      

    protected int getVariableName() {
        return 8;
    }
    
    protected byte[] prepareBuild() throws IOException {
        return null;
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        DataContainer dc = new DataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaranPlus().getLogger());
        DateType dt = new DateType(dc.getRoot().getLong(0), getTrimaranObjectFactory().getTrimaranPlus().getTimeZone());
        setDate(dt.getCalendar().getTime());
    }    
    
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
