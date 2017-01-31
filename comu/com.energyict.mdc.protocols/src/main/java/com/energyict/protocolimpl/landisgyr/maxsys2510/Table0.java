/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

class Table0 {

    TypeMaximumValues typeMaximumValues;
    
    static Table0 parse( Assembly a ){
        Table0 t = new Table0();
        t.typeMaximumValues = TypeMaximumValues.parse( a );
        return t;
    }
    
    TypeMaximumValues getTypeMaximumValues( ){
        return typeMaximumValues;
    }
    
}
