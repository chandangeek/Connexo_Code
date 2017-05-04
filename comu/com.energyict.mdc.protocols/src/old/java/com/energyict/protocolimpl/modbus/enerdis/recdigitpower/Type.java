/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.modbus.enerdis.recdigitpower;

class Type {

    private int type;
    private String description;
    
    public final static Type WORD               = new Type( 0x04, "Word" );
    public final static Type LONG_WORD          = new Type( 0x08, "Long Word" );
    public final static Type SIGNED_LONG_WORD   = new Type( 0x08, "Signed Long Word" );
    public final static Type DATE               = new Type( 0x40, "Date" );
    
    public final static Type DATE_AND_SIGNED_LONG_WORD      
        = new Type( DATE.type | SIGNED_LONG_WORD.type, "Date and Signed Long Word" );
    
    
    private Type(int type, String description){
        this.type = type;
        this.description = description;
    }
    
    int wordSize( ){
        
        int result = 0;
        
        if( (type & WORD.type)             > 0 ) result += 1;
        if( (type & LONG_WORD.type)        > 0 ) result += 2;
        if( (type & SIGNED_LONG_WORD.type) > 0 ) result += 2;
        if( (type & DATE.type)             > 0 ) result += 4;
            
        return result;
        
    }
    
    int intValue(){
        return type;
    }
    
    public String toString( ){
        return description;
    }
    
}
