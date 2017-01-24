/*
 * RealTime.java
 *
 * Created on 8 december 2006, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class RealTime extends AbstractDataDefinition {

    private Date currentDateTime; // EXTENDED_DATE_AND_TIME,
    private boolean inDST; // BOOLEAN
    private int dayOfWeek; // UNSIGNED8 0 = Monday, 1 = Tuesday etc..

    /** Creates a new instance of RealTime */
    public RealTime(DataDefinitionFactory dataDefinitionFactory) {
        super(dataDefinitionFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("RealTime:\n");
        strBuff.append("   currentDateTime="+getCurrentDateTime()+"\n");
        strBuff.append("   dayOfWeek="+getDayOfWeek()+"\n");
        strBuff.append("   inDST="+isInDST()+"\n");
        return strBuff.toString();
    }

    protected int getVariableName() {
        return 0x0012; // 18 READ DLMS_REAL_TIME
    }

    protected void parse(byte[] data) throws IOException {
        int offset=0;
        setCurrentDateTime(Utils.getDateFromDateTimeExtended(data,offset,getDataDefinitionFactory().getProtocolLink().getProtocol().getTimeZone()));
        offset+=Utils.getDateTimeExtendedSize();
        setInDST(ProtocolUtils.getInt(data,offset++,1) == 1);
        setDayOfWeek(ProtocolUtils.getInt(data,offset++,1));

    }

    public Date getCurrentDateTime() {
        return currentDateTime;
    }

    public void setCurrentDateTime(Date currentDateTime) {
        this.currentDateTime = currentDateTime;
    }

    public boolean isInDST() {
        return inDST;
    }

    public void setInDST(boolean inDST) {
        this.inDST = inDST;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
}
