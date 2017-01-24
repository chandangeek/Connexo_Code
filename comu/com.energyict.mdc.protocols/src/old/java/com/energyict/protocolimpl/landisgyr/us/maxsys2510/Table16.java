package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

class Table16 {
        
    ArrayList displayItems = new ArrayList();
    
    static Table16 parse( Assembly assembly ) throws IOException{
        Table16 t16 =  new Table16();

        while( assembly.hasMoreElements() ){
            t16.displayItems.add( TypeDisplayItemRecord.parse( assembly ) );
        }
        
        return t16;
    }
    
    /** 
     * @param index one based index 
     * @return TypeDisplayItemRecord
     */
    TypeDisplayItemRecord get( int index ){
        return (TypeDisplayItemRecord)displayItems.get(index-1);
    }
    
    public String toString() {
        StringBuffer rslt = new StringBuffer();
        Iterator i = displayItems.iterator();
        while (i.hasNext()) {
            TypeDisplayItemRecord element = (TypeDisplayItemRecord) i.next();
            rslt.append( element.toString() + " \n" );
        }
        return rslt.toString();
    }
    
}
