/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

public class IonFloat extends IonObject {
    IonFloat( float aFloat ) {
        value = new Float( aFloat );
        this.type = "IonFloat";
    }
    
    float getFloatValue(){
        return ((Float)value).floatValue();
    }
    
    boolean isFloat( ) {
        return true;
    }
    
    public String toString( ){
        return ((Float)value).toString(); 
    }
}
