/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

class Message {

    int service;
    int status;
    int pid;
    int freq;
    int priority;
    int length;

    ByteArray data;
    
    Message() {}
    
    static Message parse( Assembly assembly ){

        Message message = new Message();
        
        message.service = assembly.unsignedIntValue(2);
        boolean isResponse = (message.service == 0xe);
        
        if(isResponse)
            message.status = assembly.unsignedIntValue(1);
        message.pid = assembly.unsignedIntValue(2);
        message.freq = assembly.unsignedIntValue(1);
        message.priority = assembly.unsignedIntValue(1);
        message.length = assembly.unsignedIntValue(2);
        
        message.data = new ByteArray( assembly.byteValues(message.length) );
        
        return message;
    }

    Message setEndProgram(boolean flag) {
        if (flag)
            service = service | 0x8;
        else
            service = service & ~0x8;
        return this;
    }

    Message setExecuteProgram(boolean flag) {
        if (flag)
            service = service | 0x4;
        else
            service = service & ~0x4;
        return this;
    }

    Message setStartProgram(boolean flag) {
        if (flag)
            service = service | 0x2;
        else
            service = service & ~0x2;
        return this;
    }

    Message setResponse(boolean flag) {
        if (flag)
            service = service & ~0x1;
        else
            service = service | 0x1;
        return this;
    }

    boolean isEndProgram() {
        return ( service & 0x8 ) >  0;
    }

    boolean isExecuteProgram() {
        return ( service & 0x4 ) > 0;
    }

    boolean isStartProgram() {
        return ( service & 0x2 ) > 0;
    }

    boolean isResponse() {
        return ( service & 0x1 ) > 0;
    }
    
    boolean hasExecutedNormally( ){
        return status == 0;
    }
    
    boolean hasErrorExecutingProgram( ){
        return status == 1;
    }
    
    boolean isTimeOut( ) {
        return status == 2;
    }
    
    boolean hasBadProgramId( ){
        return status == 3;
    }

    int getStatus() {
        return status;
    }

    void setStatus(int status) {
        this.status = status;
    }

    int getLength() {
        return length;
    }

    void setLength(int length) {
        this.length = length;
    }

    int getPid( ){
        return pid;
    }
    
    int getDataStreamLength( ){
        return length;
    }
    
    Message setData( ByteArray data ) {
        this.data = data;
        this.length = data.size();
        return this;
    }
    
    ByteArray getData( ) {
        return data;
    }
    
    Message addCommand( Command cmd ) {
        if( data == null ) {
            data = new ByteArray( ).add( (byte) 0xf6 );
        }
        data.add( cmd.toByteArray() );
        
        this.length = data.size();
        
        return this;
    }
    
    ByteArray toByteArray( ){
        ByteArray rslt = new ByteArray();
        rslt.add( new byte [] {
            (byte)(service >> 8),
            (byte)(service&0x00ff),
            (byte)0x00,                 // pid
            (byte)0x00,                 // pid
            (byte)0x01,                 // frequency
            (byte)0x00,                 // priority
            (byte)(length >> 8),
            (byte)(length&0x00ff)
        } );
        if( data != null )
            rslt.add( data );
        return rslt;
    }
    
}


