package com.energyict.genericprotocolimpl.lgadvantis;

class InstallationType {
 
    public final static InstallationType COLLECTIVE = 
        new InstallationType( "collective" );
    
    public final static InstallationType INDIVIDUAL = 
        new InstallationType( "individual" );
    
    public final static InstallationType UNKNOWN = 
        new InstallationType( "unknown" );
    
    
    public static InstallationType get(String name) {
        
        if( COLLECTIVE.name.equals(name) )  return COLLECTIVE;
        if( INDIVIDUAL.name.equals(name) )  return INDIVIDUAL;
        if( UNKNOWN.name.equals(name) )     return UNKNOWN;
        
        return null;
        
    }
    
    private String name;
    
    private InstallationType(String name) {
        this.name = name;
    }
    
    public String toString( ){
        return "InstallationType [name=" + name + "]";
    }
    
}
