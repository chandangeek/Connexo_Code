package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;

class Table11 {

    TypeStoreCntrlRcd typeStoreCntrlRcd;
    
    static Table11 parse( MaxSys maxSys, Assembly a ) throws IOException{
        Table11 t = new Table11();
        t.typeStoreCntrlRcd = TypeStoreCntrlRcd.parse( maxSys, a );
        return t;
    }
    
    TypeStoreCntrlRcd getTypeStoreCntrlRcd( ){
        return typeStoreCntrlRcd;
    }
    
    public String toString() {
        return typeStoreCntrlRcd.toString();
    }
    
}
