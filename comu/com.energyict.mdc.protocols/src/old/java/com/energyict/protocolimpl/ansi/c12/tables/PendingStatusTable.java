/*
 * PendingStatusTable.java
 *
 * Created on 26 oktober 2005, 10:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
import java.util.Date;
/**
 *
 * @author Koen
 */
public class PendingStatusTable extends AbstractTable {

    private byte[] standardPending;
    private byte[] manufacturerPending;
    private Date lastActivationTimeDate;
    private int nrOfPendingActivations;
    private EntryActivation[] entryActivation;

    /** Creates a new instance of PendingStatusTable */
    public PendingStatusTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(4));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PendingStatusTable: \n");
        strBuff.append("    standardPending="+ProtocolUtils.getResponseData(getStandardPending())+", manufacturerPending="+ProtocolUtils.getResponseData(getManufacturerPending())+"\n");
        strBuff.append("    lastActivationTimeDate="+getLastActivationTimeDate()+" nrOfPendingActivations="+getNrOfPendingActivations()+"\n");
        for (int i=0;i<entryActivation.length;i++) {
            strBuff.append("entryActivation["+i+"]="+getEntryActivation()[i]+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        int offset=0;
        ConfigurationTable ct = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setStandardPending(new byte[ct.getDimStdTablesUsed()]);
        setStandardPending(ProtocolUtils.getSubArray2(tableData, offset, getStandardPending().length));
        offset+=getStandardPending().length;
        setManufacturerPending(new byte[ct.getDimMfgTablesUsed()]);
        setManufacturerPending(ProtocolUtils.getSubArray2(tableData, offset, getManufacturerPending().length));
        offset+=getManufacturerPending().length;
        if (tableFactory.getC12ProtocolLink().getManufacturer().getMeterProtocolClass().compareTo("com.energyict.protocolimpl.itron.sentinel.Sentinel")==0)
            setLastActivationTimeDate(C12ParseUtils.getDateFromSTimeAndAdjustForTimeZone(tableData, offset, ct.getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));
        else
            setLastActivationTimeDate(C12ParseUtils.getDateFromSTime(tableData, offset, ct.getTimeFormat(), getTableFactory().getC12ProtocolLink().getTimeZone(),dataOrder));

        offset+=C12ParseUtils.getSTimeSize(ct.getTimeFormat());
        setNrOfPendingActivations(C12ParseUtils.getInt(tableData,offset));
        offset++;
        setEntryActivation(new EntryActivation[ct.getNrPending()]);
        for (int i=0;i<ct.getNrPending();i++) {
            byte[] subTableData = ProtocolUtils.getSubArray2(tableData, offset, EntryActivation.SIZE);
            offset+=EntryActivation.SIZE;
            getEntryActivation()[i] = new EntryActivation(subTableData,dataOrder);
        }

    }

    public byte[] getStandardPending() {
        return standardPending;
    }

    public void setStandardPending(byte[] standardPending) {
        this.standardPending = standardPending;
    }

    public byte[] getManufacturerPending() {
        return manufacturerPending;
    }

    public void setManufacturerPending(byte[] manufacturerPending) {
        this.manufacturerPending = manufacturerPending;
    }

    public Date getLastActivationTimeDate() {
        return lastActivationTimeDate;
    }

    public void setLastActivationTimeDate(Date lastActivationTimeDate) {
        this.lastActivationTimeDate = lastActivationTimeDate;
    }

    public int getNrOfPendingActivations() {
        return nrOfPendingActivations;
    }

    public void setNrOfPendingActivations(int nrOfPendingActivations) {
        this.nrOfPendingActivations = nrOfPendingActivations;
    }

    public EntryActivation[] getEntryActivation() {
        return entryActivation;
    }

    public void setEntryActivation(EntryActivation[] entryActivation) {
        this.entryActivation = entryActivation;
    }

}
