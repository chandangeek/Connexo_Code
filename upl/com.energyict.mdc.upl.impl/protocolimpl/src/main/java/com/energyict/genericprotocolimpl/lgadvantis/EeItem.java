package com.energyict.genericprotocolimpl.lgadvantis;

class EeItem {
    
    private String ident;
    private Long paramId;
    private String configId;
    private String utilityId;
    
    EeItem(String ident) {
        this.ident = ident;
    }
    
    EeItem(String ident, long paramId, String configId, String utilityId) {
        this( ident );
        
        this.paramId = new Long( paramId );
        this.configId = configId;
        this.utilityId = utilityId;
    }
    
    String getIdent( ) {
        return ident;
    }
    
    void setParamId( Long pi ) {
        this.paramId = pi;
    }
    
    void setConfigId(String configId) {
        this.configId = configId;
    }
    
    void setUtilityId(String utilityId) {
        this.utilityId = utilityId;
    }
    
    public String toString( ){
        return "EeItem [" +
        		    "ident="      + ident     + ", " +
        		    "paramId="    + paramId   + ", " +
        		    "configId="   + configId  + ", " +
        		    "utilityId="  + utilityId +
        		"]";
    }
    
}
