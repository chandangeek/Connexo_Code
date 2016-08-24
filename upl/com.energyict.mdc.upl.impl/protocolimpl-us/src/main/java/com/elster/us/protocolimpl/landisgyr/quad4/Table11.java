package com.elster.us.protocolimpl.landisgyr.quad4;

import java.io.IOException;

class Table11 {

    TypeStoreCntrlRcd typeStoreCntrlRcd;
    
    static Table11 parse(Quad4 quad4, Assembly a ) throws IOException{
        Table11 t = new Table11();
        t.typeStoreCntrlRcd = TypeStoreCntrlRcd.parse(quad4, a );
        return t;
    }
    
    TypeStoreCntrlRcd getTypeStoreCntrlRcd( ){
        return typeStoreCntrlRcd;
    }
    
    public String toString() {
        return typeStoreCntrlRcd.toString();
    }
    
}
