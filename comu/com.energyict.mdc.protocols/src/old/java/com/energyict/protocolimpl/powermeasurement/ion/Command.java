/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

public class Command {

    IonHandle handle;
    IonMethod method;
    ByteArray arguments;
    
    IonObject response;
    
    Command( IonHandle handle, IonMethod method ) {
        this.handle = handle;
        this.method = method;
    }
    
    IonHandle getHandle( ) {
        return handle;
    }
    
    IonMethod getMethod( ){
        return method;
    }
    
    Command setArguments( ByteArray arguments ) {
        this.arguments = arguments;
        return this;
    }
    
    ByteArray toByteArray( ){
        ByteArray rslt = new ByteArray( );
        rslt.add( handle.toByteArray() )
            .add( method.toByteArray() );
        if( arguments != null )
            rslt.add( arguments );
        return rslt;
    }
    
    void setResponse( IonObject ionObject ) {
        response = ionObject;
    }
    
    IonObject getResponse( ){
        return response;
    }
    
    public String toString( ){
        return 
            new StringBuffer()
                .append( "Command [ " ).append( handle )
                .append( ", " ).append( method )
                .append( ", response " ).append( response )
                .append( " ]" ).toString();
    }

    /**
     * @return the arguments
     */
    public ByteArray getArguments(){
        return this.arguments;
    }

}
