package com.energyict.protocolimpl.enermet.e120;

class Request implements MessageBody {
    
    ByteArray body;
    
    Request( ){
        this(new ByteArray());
    }
    
    Request(ByteArray body){
        this.body = body;
    }
    
    ByteArray getBody(){
        return body;
    }
    
    ByteArray toByteArray(){
        return body;
    }
    
    public String toString(){
        return body.toHexaString(true);
    }
    
}
