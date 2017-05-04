/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

/**
 * BUS_SELECT : BYTE; 
 * { Select bus: 
 *      I: MTR_INPUT_BUS 
 *      T: TOTALIZATION_BUS 
 *      K: KOMPENSATION_BUS 
 *      X: XFORM_BUS 
 *      R: RATE_BUS 
 *      C: CONTROL_TOU_BUS 
 *      L: LOAD_CNTRL_BUS 
 *      D: DISPLAY_BUS 
 *      M: MISC_FUNC_BUS 
 *      S: SUMM_CH_BUS 
 * }
 * 
 * LINE_SELECT : BYTE 
 * { Select line number 1 to 255 line no's. correspond to subscript no where 
 * applicable.  MISC_FUNC_BUS lines vary with the manufacturer and must be 
 * known by the Central Computer. Examples of misc functions
 * include End of Interval pulses, pulses during particular rating periods, etc. }
 * 
 */

class TypeChannelSelectRcd {

    /** datablock index (1 based) */
    int datablockIndex;
    /** row index within datablock */
    int rowIndex;
    /** TableAddress for directly accessing a table. */
    TableAddress tableAddress;
    
    Bus bus;
    LineSelect line;

    static TypeChannelSelectRcd parse(Assembly assembly, int databockIndex, int rowIndex ) {
        TypeChannelSelectRcd chnl = new TypeChannelSelectRcd();
        chnl.datablockIndex = databockIndex;
        chnl.rowIndex = rowIndex;
        int i = assembly.rawByteValue();
        chnl.bus = Bus.get( (char)i );
        i = assembly.rawByteValue();
        chnl.line = LineSelect.get(i);
        return chnl;
    }

    int getDataBlockIndex( ){
        return datablockIndex;
    }
    
    int getRowIndex( ){
        return rowIndex;
    }
    
    TableAddress getTableAddress( ){
        return tableAddress;
    }
    
    TableAddress getTableAddress(int offset){
        return this.tableAddress.copy(offset);
    }
    
    void setTableAddress( TableAddress tableAddress ){
        this.tableAddress = tableAddress;
    }
    
    Bus getBus() {
        return bus;
    }

    LineSelect getLine() {
        return line;
    }

    public String toString() {
        StringBuffer rslt = new StringBuffer();
        rslt.append( "( dataBlockIndex " + datablockIndex + ", " );
        rslt.append( " rowIndex " + rowIndex + ", " );
        rslt.append( tableAddress + ", " );
        rslt.append( "b " + bus + ", " );
        rslt.append( "l " + line + ", " );
        rslt.append( " )" );
        return rslt.toString();
    }

}
