package com.energyict.protocolimpl.landisgyr.maxsys2510;

class Table3 {

    TypeStatusRcd typeStatusRcd;
    
    static Table3 parse( MaxSys maxSys, Assembly a ){
        Table3 t = new Table3();
        t.typeStatusRcd = TypeStatusRcd.parse( maxSys, a );
        return t;
    }
    
    TypeStatusRcd getTypeStatusRcd( ){
        return typeStatusRcd;
    }
    
    
    
}
