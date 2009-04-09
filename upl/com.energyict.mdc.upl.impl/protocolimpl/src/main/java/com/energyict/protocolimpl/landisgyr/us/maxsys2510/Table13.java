package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
        StringBuffer rslt = new StringBuffer();
        rslt.append( "Table13 [ \n " );
        rslt.append( touSchedRcd.toString() );
        rslt.append( "]" );
        return rslt.toString();
    }
    
    public static void main( String [] args ) throws FileNotFoundException, IOException{
        File f = new File( "c:\\dump" );
        byte buffer [] = new byte[(int)f.length()];
        new FileInputStream( f ).read( buffer );
        MaxSys mSys = new MaxSys();
        Table13 t = Table13.parse(new Assembly( mSys, new ByteArray( buffer ) ) );
        System.out.println( t );
    }
    
}
