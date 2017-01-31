/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

class IonChar extends IonObject {
    
    IonChar( char aChar ) {
        value = new Character( aChar );
        this.type = "IonChar";
    }
    
    char getCharValue(){
        return ((Character)value).charValue();
    }
    
    public String toString( ){
        return ((Character)value).toString(); 
    }
    
}