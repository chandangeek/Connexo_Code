package com.energyict.genericprotocolimpl.lgadvantis;

import java.util.*;

class Bcpl {
 
    private String ident;
    private String address;
    private long credit;
    private InstallationType installationType;
    
    private List eeItems = new ArrayList();
    
    public Bcpl(String ident, String address, long credit, InstallationType installType) {
        this.ident = ident;
        this.address = address;
        this.credit = credit;
        this.installationType = installType;
    }
    
    void addEeItem(EeItem eeItem){
        eeItems.add(eeItem);
    }
    
    List getEeItems( ){
        return Collections.unmodifiableList(eeItems);
    }
    
    public String toString( ){
        
        StringBuffer result = 
            new StringBuffer()
                .append( "Bcpl[ ident=" + ident + ", " )
                .append( "address=" + address + ", " ) 
                .append( "credit=" + credit + ", " )
                .append( installationType );       
        
        if( eeItems.size() > 0 )
            result.append( "\n" );
        
        Iterator i = eeItems.iterator();
        while( i.hasNext() ) {
            result.append( i.next() + " \n" );
        }
                
        result.append( "]" );
        return result.toString();
        
    }
    
}
