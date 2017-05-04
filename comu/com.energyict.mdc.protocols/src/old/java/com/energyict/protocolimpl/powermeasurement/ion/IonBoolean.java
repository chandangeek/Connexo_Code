/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

class IonBoolean extends IonObject {
    
    IonBoolean( boolean value ){
        this.value = new Boolean( value );
        this.type = "IonBoolean";
    }
    
    boolean getBooleanValue( ){
        return ((Boolean)value).booleanValue();
    }
    
    public String toString( ){
        return ((Boolean)value).toString(); 
    }
    
}