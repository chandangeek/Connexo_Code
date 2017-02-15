/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class Table13 {

    TypeTouSchedRcd touSchedRcd;

    static Table13 parse( Assembly assembly ) throws IOException{

        File f = new File( "c:\\dump" );
        FileOutputStream fos = new FileOutputStream(f);
        fos.write( assembly.toBytes() );
        fos.close();

        Table13 t = new Table13();
        t.touSchedRcd = TypeTouSchedRcd.parse( assembly );
        return t;
    }

    TypeTouSchedRcd getTouSchedRcd() {
        return touSchedRcd;
    }

    public String toString( ){
        return "Table13 [ \n " + touSchedRcd.toString() + "]";
    }

}