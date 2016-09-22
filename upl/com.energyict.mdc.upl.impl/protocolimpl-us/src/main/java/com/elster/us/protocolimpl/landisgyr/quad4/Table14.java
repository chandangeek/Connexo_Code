package com.elster.us.protocolimpl.landisgyr.quad4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class Table14 {

    /** list of all datablocks */
    ArrayList dataBlkCntrl;
    
    /** list of */
    ArrayList summations = new ArrayList();
    /** list of */
    ArrayList rates = new ArrayList();
    
    static Table14 parse( Assembly assembly ) throws IOException{
        
        TypeMaximumValues tmv = assembly.getQuad4().getTable0().getTypeMaximumValues();
        int byteOffset = 8; 
        
        Table14 t14 = new Table14();
        t14.dataBlkCntrl = new ArrayList();
        int datablockIndex = tmv.getMaxDataBlks();


        for (int i = 0; i < datablockIndex; i++) {
            DataBlkCntrl dbc = DataBlkCntrl.parse(assembly, byteOffset, i+1);
            t14.dataBlkCntrl.add( dbc );
            List summations = dbc.getSummations();
            for (int count = 0; count < summations.size(); count++) {
                System.out.println("DBI: " + i + ", summation: " + summations.get(count));
            }
            t14.summations.addAll( dbc.getSummations() );
            t14.rates.addAll( dbc.getRates() );
            byteOffset = dbc.getDataBlkRcdByteSize();


        }
        return t14;

    }
    
    List getAllSummations( ){
        return summations;
    }
    
    List getSummations( Bus bus, LineSelect line ) {
        ArrayList rslt = new ArrayList();
        Iterator i = summations.iterator();
        while( i.hasNext() ) {
            TypeChannelSelectRcd tcs = (TypeChannelSelectRcd)i.next();
            //System.out.println("Looking at  TCS: " + tcs + " for bus " + bus + " and line " + line);
            if( tcs.bus != null && tcs.bus.equals( bus ) && tcs.getLine().equals( line ) ) {
                //System.out.println("Match");
                rslt.add( tcs );
            } else {
                //System.out.println("No match");
            }
        }
        return rslt;
    }
    
    List getRates( Bus bus, LineSelect line ){
        ArrayList rslt = new ArrayList();
        Iterator i = rates.iterator();
        while( i.hasNext() ) {
            TypeChannelSelectRcd tcs = (TypeChannelSelectRcd)i.next();
            if( tcs.bus != null && tcs.bus.equals( bus ) && tcs.getLine().equals( line ) )
                rslt.add( tcs );
        }
        return rslt;
    }
    
    List getDataBlkCntrl( ){
        return dataBlkCntrl;
    }
    
    public String toString( ){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "Table14 [ \n" );
        
        Iterator i = null;

        i = dataBlkCntrl.iterator();
        while( i.hasNext() ){
            DataBlkCntrl dbc = (DataBlkCntrl)i.next();
            rslt.append( dbc.toString() + "\n" );
        }
        
        rslt.append( "]" );
        return rslt.toString();
    }
  
}
