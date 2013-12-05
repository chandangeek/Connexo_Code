/*
 * OptionBoardScratchPad.java
 *
 * Created on 13/02/2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.tables.AbstractTable;
import com.energyict.protocolimpl.ansi.c12.tables.TableIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class OptionBoardScratchPad extends AbstractTable {
    /*
    Memory storage: EEPROM
    Total table size: (bytes) 200
    Read access: 1
    Write access: 3

    This table is not used by the meter but provides nonvolatile memory for option boards. If you
    desire to use MT-87, please consult Elster Electricity first for suggested guidelines before
    attempting to store data in this table.
     */

    /** Creates a new instance of OptionBoardScratchPad */
    public OptionBoardScratchPad(ManufacturerTableFactory manufacturerTableFactory) {
        super(manufacturerTableFactory,new TableIdentification(87,true));
    }



    protected void parse(byte[] tableData) throws IOException {
        int offset = 0;
    }

    private ManufacturerTableFactory getManufacturerTableFactory() {
        return (ManufacturerTableFactory)getTableFactory();
    }


}
