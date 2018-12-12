/*
 * CustomerNotes.java
 *
 * Created on 7 juli 2004, 12:19
 */

package com.energyict.protocolimpl.iec1107.indigo;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class CustomerNotes extends AbstractLogicalAddress {
    
    String note;
    
    /** Creates a new instance of CustomerNotes */
    public CustomerNotes(int id,int size, LogicalAddressFactory laf) throws IOException {
        super(id,size,laf);
    }
    
    public String toString() {
       return "CustomerNotes: "+getNote();    
    }
    
    public void parse(byte[] data, java.util.TimeZone timeZone) throws IOException {
        setNote(new String(data));
    }
    
    /**
     * Getter for property note.
     * @return Value of property note.
     */
    public java.lang.String getNote() {
        return note;
    }
    
    /**
     * Setter for property note.
     * @param note New value of property note.
     */
    public void setNote(java.lang.String note) {
        this.note = note;
    }
    
}
