/*
 * UnitOfMeasureEntryTable.java
 *
 * Created on 28 november 2005, 14:52
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class UnitOfMeasureEntryTable extends AbstractTable {

    private UOMEntryBitField[] uomEntryBitField;

    /** Creates a new instance of UnitOfMeasureEntryTable */
    public UnitOfMeasureEntryTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(12));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("UnitOfMeasureEntryTable: \n");
        for (int i=0;i<getUomEntryBitField().length;i++)
            strBuff.append("uomEntryBitField["+i+"]="+getUomEntryBitField()[i]+"\n");
        return strBuff.toString();
    }

    protected void prepareBuild() throws IOException {
        int size = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getMaxNrOfEntriesUOMEntry() * UOMEntryBitField.getSize();
        PartialReadInfo partialReadInfo = new PartialReadInfo(0,size);
        setPartialReadInfo(partialReadInfo);
    }


    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        setUomEntryBitField(new UOMEntryBitField[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getMaxNrOfEntriesUOMEntry()]);
        for (int i=0;i<getUomEntryBitField().length;i++) {
            getUomEntryBitField()[i] = new UOMEntryBitField(tableData, offset, getTableFactory());
            offset+=UOMEntryBitField.getSize();
        }



    }

    public UOMEntryBitField[] getUomEntryBitField() {
        return uomEntryBitField;
    }

    public void setUomEntryBitField(UOMEntryBitField[] uomEntryBitField) {
        this.uomEntryBitField = uomEntryBitField;
    }
}
