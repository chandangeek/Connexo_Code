/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.enermet.e120;

class DefaultResponse implements MessageBody, Response {
    
    private NackCode nackCode;
    private Object value;

    public DefaultResponse(NackCode nackCode) {
        this.nackCode = nackCode;
    }
    
    public NackCode getNackCode(){
        return nackCode;
    }
    
    public boolean isOk() {
        return NackCode.OK.equals(nackCode);
    }

    public void setValue(Object value){
        this.value = value;
    }
    
    public Object getValue() {
        return value;
    }

    public String toString(){
        StringBuffer rslt = new StringBuffer();
        rslt.append( "DefaultResponse [" );
        rslt.append( nackCode.toString() );
        
        if(value!=null)
            rslt.append( ", " + value );
        
        rslt.append( "]" );
        rslt.toString();
        
        return rslt.toString();
    }

}
