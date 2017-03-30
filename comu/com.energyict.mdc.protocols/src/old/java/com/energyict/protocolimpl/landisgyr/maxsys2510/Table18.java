/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

class Table18 {

    ArrayList displayItems = new ArrayList();
    
    static Table18 parse( Assembly assembly  ) throws IOException{
        Table18 t18 = new Table18();
        Table16 t16 = assembly.getMaxSys().getTable16();
        
        assembly.wordValue(); // delay value, ingnore
        while( assembly.hasMoreElements() ) {
            int index = assembly.wordValue();
            if( index == 0xffff )
                break;
            t18.displayItems.add( t16.get( index ) );
        } 
        
        return t18; 
    }

    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "Table18 [\n");
        
        Iterator i = displayItems.iterator();
        while( i.hasNext() ) {
            
            rslt.append( i.next().toString() + "\n" );
        }
        rslt.append( "]" );
        return rslt.toString();
    }
    
}
