package com.elster.us.protocolimpl.landisgyr.quad4;

class Table3 {

    TypeStatusRcd typeStatusRcd;
    
    static Table3 parse(Quad4 quad4, Assembly a ){
        Table3 t = new Table3();
        t.typeStatusRcd = TypeStatusRcd.parse(quad4, a );
        return t;
    }
    
    TypeStatusRcd getTypeStatusRcd( ){
        return typeStatusRcd;
    }
    
    
    
}
