package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.*;
import java.sql.SQLException;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.eisimport.core.AbstractImporter;

public class EventImporter extends AbstractImporter {

    protected void doImportFile() throws BusinessException, SQLException, IOException {

        System.out.println("zeker");        
        
        long length = getFile().length();
        
        if( length > Integer.MAX_VALUE ) {
            getLogger().severe("File is too large");
            return;
        } 
        
        byte [] buffer = new byte [(int)length];
        new FileInputStream( getFile() ).read(buffer);

        System.out.println( AXDRDecoder.decode(buffer) );
        
    }

    
    public static void main( String [] args ) throws FileNotFoundException, IOException{
        
        File f = new File( "c:\\import2\\070798001234_0-0-10-0-130-255_02_20070827-1527121.cos" );
        long length = f.length();

        byte [] buffer = new byte [(int)length];
        new FileInputStream( f ).read(buffer);

        System.out.println( AXDRDecoder.decode(buffer) );

        
    }
    
    
}
