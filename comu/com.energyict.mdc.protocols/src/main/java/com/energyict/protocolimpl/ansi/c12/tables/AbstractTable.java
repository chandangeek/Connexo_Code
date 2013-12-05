/*
 * AbstractTable.java
 *
 * Created on 18 oktober 2005, 11:59
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.PartialReadInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
/**
 *
 * @author Koen
 */
abstract public class AbstractTable {

    abstract protected void parse(byte[] data) throws IOException;

    TableFactory tableFactory;
    private TableIdentification tableIdentification;
    private byte[] tableData;
    private PartialReadInfo partialReadInfo=null;
    private PartialReadInfo partialReadInfo2=null;
    private boolean forceFullRead;

    /** Creates a new instance of AbstractTable */
    public AbstractTable(TableFactory tableFactory,TableIdentification tableIdentification) {
        this.tableFactory=tableFactory;
        this.tableIdentification=tableIdentification;
        setForceFullRead(false);
    }

    protected TableFactory getTableFactory() {
        return tableFactory;
    }


    protected TableIdentification getTableIdentification() {
        return tableIdentification;
    }

    protected void prepareBuild() throws IOException {
        // override to provide extra functionality...
    }

    public void build() throws IOException {
        prepareBuild();
        if ((isForceFullRead()) || (getPartialReadInfo() == null))
            parse(getTableFactory().getC12ProtocolLink().getPSEMServiceFactory().fullRead(getTableIdentification().getTableId()));
        else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int offset=getPartialReadInfo().getOffset();
            int count=getPartialReadInfo().getCount();
            while(count>0) {
                byte[] responseData = getTableFactory().getC12ProtocolLink().getPSEMServiceFactory().partialReadOffset(getTableIdentification().getTableId(), offset, count);
                count-=responseData.length;
                offset+=responseData.length;
                baos.write(responseData);
            }

            // if we need to retrieve a non-continuous second block of data in the same table...
            if (getPartialReadInfo2() != null) {
                offset=getPartialReadInfo2().getOffset();
                count=getPartialReadInfo2().getCount();
                while(count>0) {
                    byte[] responseData = getTableFactory().getC12ProtocolLink().getPSEMServiceFactory().partialReadOffset(getTableIdentification().getTableId(), offset, count);
                    count-=responseData.length;
                    offset+=responseData.length;
                    baos.write(responseData);
                }
            }

            parse(baos.toByteArray());
        }
    }

    protected void prepareTransfer() throws IOException {
        // override to provide extra functionality...
        setTableData(null);
    }

    public void transfer()  throws IOException {
        prepareTransfer();
        getTableFactory().getC12ProtocolLink().getPSEMServiceFactory().fullWrite(getTableIdentification().getTableId(), tableData);
    }

    public byte[] getTableData() {
        return tableData;
    }

    public void setTableData(byte[] tableData) {
        this.tableData = tableData;
    }

    public PartialReadInfo getPartialReadInfo() {
        return partialReadInfo;
    }

    public void setPartialReadInfo(PartialReadInfo partialReadInfo) {
        this.partialReadInfo = partialReadInfo;
    }

    public boolean isForceFullRead() {
        return forceFullRead;
    }

    public void setForceFullRead(boolean forceFullRead) {
        this.forceFullRead = forceFullRead;
    }

    public PartialReadInfo getPartialReadInfo2() {
        return partialReadInfo2;
    }

    public void setPartialReadInfo2(PartialReadInfo partialReadInfo2) {
        this.partialReadInfo2 = partialReadInfo2;
    }

}
