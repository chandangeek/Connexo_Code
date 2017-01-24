package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;

class Table15 {

    TouDataRcd touDataRcd;
    
    static Table15 parse( Assembly assembly ) throws IOException{
        Table15 t = new Table15();
        t.touDataRcd = TouDataRcd.parse(assembly);
        return t;
    }

    TouDataRcd getTouDataRcd() {
        return touDataRcd;
    }
    
    public String toString( ){
        return touDataRcd.toString();
    }
    
}