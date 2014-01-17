/*
 * LoadProfileStartBlock.java
 *
 * Created on 9 december 2005, 21:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.procedures;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.procedures.AbstractProcedure;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureIdentification;
import com.energyict.protocolimpl.base.ParseUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
/**
 *
 * @author Koen
 */
public class LoadProfileStartBlock extends AbstractProcedure {

    private Date startTimeDate;

    private int startingBlockOffset;

    /** Creates a new instance of SnapShotData */
    public LoadProfileStartBlock(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(22,true));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileStartBlock:\n");
        strBuff.append("   startTimeDate="+getStartTimeDate()+"\n");
        return strBuff.toString();
    }

    protected void parse(byte[] data) throws IOException {
        int dataOrder = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset=0;
        setStartingBlockOffset((int)data[0]&0xFF);
    }

    protected void prepare() throws IOException {

        Calendar cal = ProtocolUtils.getCleanCalendar(getProcedureFactory().getC12ProtocolLink().getTimeZone());
        cal.set(Calendar.YEAR,2000);
        cal.set(Calendar.MONTH,0);
        cal.set(Calendar.DAY_OF_MONTH,1);
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);

        long secondsSince2000 = (getStartTimeDate().getTime()-cal.getTime().getTime())/1000;

        int dataOrder = getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        if (dataOrder == 1)
            setProcedureData(ParseUtils.getArray(secondsSince2000,4));
        else
            setProcedureData(ParseUtils.getArrayLE(secondsSince2000,4));
    }

    public Date getStartTimeDate() {
        return startTimeDate;
    }

    public void setStartTimeDate(Date startTimeDate) {
        this.startTimeDate = startTimeDate;
    }

    public int getStartingBlockOffset() {
        return startingBlockOffset;
    }

    public void setStartingBlockOffset(int startingBlockOffset) {
        this.startingBlockOffset = startingBlockOffset;
    }



}
