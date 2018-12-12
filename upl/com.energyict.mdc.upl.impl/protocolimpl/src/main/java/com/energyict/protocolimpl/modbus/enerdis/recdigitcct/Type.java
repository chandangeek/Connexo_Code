package com.energyict.protocolimpl.modbus.enerdis.recdigitcct;

class Type {

    private int type;
    private String description;
    
    public final static Type BCD = new Type( 0x01, "bcd" );
    public final static Type UNSIGNED_SHORT = new Type(0x04, "unsigned short");
    public final static Type UNSIGNED_LONG = new Type(0x08, "unsigned long");
    public final static Type DATE = new Type( 0x10, "Date" );
    public final static Type CHAR = new Type( 0x20, "CHAR" );
    

    private Type(int type, String description) {
        this.type = type;
        this.description = description;
    }
    
    int wordSize( ){
        
        int result = 0;
        
        if( (type & BCD.type)              > 0 ) result += 1;
        if( (type & UNSIGNED_SHORT.type)   > 0 ) result += 1;
        if( (type & UNSIGNED_LONG.type)    > 0 ) result += 2;
        if( (type & DATE.type)             > 0 ) result += 4;
        if( (type & CHAR.type)			   > 0 ) result += 4;	
            
        return result;
        
    }
    
    int intValue(){
        return type;
    }
    
    public String toString( ){
        return description;
    }
    
}
