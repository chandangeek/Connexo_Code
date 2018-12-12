/**
 * 
 */
package com.energyict.protocolimpl.powermeasurement.ion;

class IonInteger extends IonObject {
    
    IonInteger( int anInt ) {
        value = new Integer( anInt );
        this.type = "IonInteger";
    }
    
    int getIntValue(){
        return ((Integer)value).intValue();
    }
    
    boolean isInteger( ) {
        return true;
    }
    
    public String toString( ){
        return ((Integer)value).toString(); 
    }
    
}