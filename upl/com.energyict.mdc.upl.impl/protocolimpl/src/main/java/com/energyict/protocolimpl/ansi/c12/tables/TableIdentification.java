/*
 * TableIdentification.java
 *
 * Created on 18 oktober 2005, 13:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

/**
 *
 * @author Koen
 */
public class TableIdentification {
    
    
    
    private int tableId;
    private boolean manufacturerTable;
    private boolean pendingTable;
    
    
    
    /** Creates a new instance of TableIdentification */
    public TableIdentification(int tableId) {
        this(tableId,false);
    }
    public TableIdentification(int tableId,boolean manufacturerTable) {
        this(tableId, manufacturerTable, false);
    }
    public TableIdentification(int tableId,boolean manufacturerTable,boolean pendingTable) {
        this.tableId=tableId;
        this.manufacturerTable=manufacturerTable;
        this.pendingTable=pendingTable;
        
        if (this.manufacturerTable)
            this.tableId |= 0x800;
        
        if (this.pendingTable)
            this.tableId |= 0x1000;
        
        if ((tableId >= 2048) && (tableId < 4096)) {
            this.manufacturerTable=true;
            this.pendingTable=false;
        }
        if ((tableId >= 4096) && (tableId < 6144)) {
            this.manufacturerTable=false;
            this.pendingTable=true;
        }
        if ((tableId >= 6144) && (tableId < 8192)) {
            this.manufacturerTable=true;
            this.pendingTable=true;
        }
        
        
        //this.tableId &= 0x007FF;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public boolean isManufacturerTable() {
        return manufacturerTable;
    }

    public void setManufacturerTable(boolean manufacturerTable) {
        this.manufacturerTable = manufacturerTable;
    }

    public boolean isPendingTable() {
        return pendingTable;
    }

    public void setPendingTable(boolean pendingTable) {
        this.pendingTable = pendingTable;
    }


}
