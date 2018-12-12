package com.energyict.protocolimpl.modbus.enerdis.recdigit1800;

class Type {

    private int type;
    private String description;
    
    public final static Type BYTE               = new Type( 0x01, "Byte" );
    public final static Type OCTET              = new Type( 0x02, "Octet" );
    public final static Type WORD               = new Type( 0x04, "Word" );
    public final static Type LONG_WORD          = new Type( 0x08, "Long Word" );
    public final static Type DOUBLE_LONG_WORD   = new Type( 0x10, "Double Long Word" );
    public final static Type REAL_NUMBER        = new Type( 0x20, "Real Number" );
    public final static Type DATE               = new Type( 0x40, "Date" );
    
    public final static Type DATE_AND_WORD      
        = new Type( DATE.type | WORD.type, "Date and Word" );
    public final static Type DATE_AND_LONG_WORD 
        = new Type( DATE.type | LONG_WORD.type, "Date and Long Word" );

    private Type(int type, String description){
        this.type = type;
        this.description = description;
    }
    
    int wordSize( ){
        
        int result = 0;
        
        if( (type & BYTE.type)             > 0 ) result += 1;
        if( (type & OCTET.type)            > 0 ) result += 1;
        if( (type & WORD.type)             > 0 ) result += 1;
        if( (type & LONG_WORD.type)        > 0 ) result += 2;
        if( (type & DOUBLE_LONG_WORD.type) > 0 ) result += 4;
        if( (type & REAL_NUMBER.type)      > 0 ) result += 4;
        if( (type & DATE.type)             > 0 ) result += 3;
            
        return result;
        
    }
    
    int intValue(){
        return type;
    }
    
    public String toString( ){
        return description;
    }
    
}
