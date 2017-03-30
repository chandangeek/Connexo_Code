/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DataControlTable.java
 *
 * Created on 26 oktober 2005, 16:00
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;



/**
 *
 * @author Koen
 */
public class DataControlTable extends AbstractTable {

    /*
     *   sourceId[nr of data control sources][manufacturer depending nr of octets]
     */
    private byte[][] sourceId;


    /** Creates a new instance of DataControlTable */
    public DataControlTable(StandardTableFactory tableFactory) {
        super(tableFactory,new TableIdentification(14));
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DataControlTable: \n");
        for (int i=0;i<getSourceId().length;i++) {
            strBuff.append("sourceId["+i+"]="+ProtocolUtils.getResponseData(getSourceId()[i])+"\n");
        }
        return strBuff.toString();
    }

    protected void parse(byte[] tableData) throws IOException {
        int offset=0;
        setSourceId(new byte[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getMaxNrOfEntriesDataControl()][getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getDataControlLength()]);
        for (int i=0;i<getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getMaxNrOfEntriesDataControl();i++) {
            //sourceId[i] = new byte[getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getDataControlLength()];
            getSourceId()[i] = ProtocolUtils.getSubArray2(tableData, offset, getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getDataControlLength());
            offset+=getTableFactory().getC12ProtocolLink().getStandardTableFactory().getActualSourcesLimitingTable().getDataControlLength();
        }
    }

    public byte[][] getSourceId() {
        return sourceId;
    }

    public void setSourceId(byte[][] sourceId) {
        this.sourceId = sourceId;
    }

}
