package com.elster.us.protocolimpl.landisgyr.quad4;

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
