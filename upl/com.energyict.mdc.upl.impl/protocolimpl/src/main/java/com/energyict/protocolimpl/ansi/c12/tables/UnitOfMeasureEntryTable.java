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

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class UnitOfMeasureEntryTable extends AbstractTable {
    
    private UOMEntryBitField[] uomEntryBitField;

    int reduceMaxNumberOfUomEntryBy = 0;

    /** Creates a new instance of UnitOfMeasureEntryTable */
    public UnitOfMeasureEntryTable(StandardTableFactory tableFactory, int reduceMaxNumberOfUomEntryBy) {
        this(tableFactory);
        this.reduceMaxNumberOfUomEntryBy = reduceMaxNumberOfUomEntryBy;
    }

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
        int size;
        int numUomEntries = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getMaxNrOfEntriesUOMEntry();
        numUomEntries -= reduceMaxNumberOfUomEntryBy;
        size = numUomEntries * UOMEntryBitField.getSize();

        PartialReadInfo partialReadInfo = new PartialReadInfo(0,size);
        setPartialReadInfo(partialReadInfo);
    }
    
    
    protected void parse(byte[] tableData) throws IOException { 
        int offset=0;
        int numUomEntries = getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getMaxNrOfEntriesUOMEntry();
        numUomEntries -= reduceMaxNumberOfUomEntryBy;
        setUomEntryBitField(new UOMEntryBitField[numUomEntries]);
        try {
            for (int i = 0; i < getUomEntryBitField().length; i++) {
                getUomEntryBitField()[i] = new UOMEntryBitField(tableData, offset, getTableFactory());
                offset += UOMEntryBitField.getSize();
            }
        } catch (ProtocolException e) {
            if (!e.getMessage().contains("ProtocolUtils, getLongLE, ArrayIndexOutOfBoundsException")) {
                throw e;
            }
        }
    }

    public UOMEntryBitField[] getUomEntryBitField() {
        return uomEntryBitField;
    }

    public void setUomEntryBitField(UOMEntryBitField[] uomEntryBitField) {
        this.uomEntryBitField = uomEntryBitField;
    }
}
