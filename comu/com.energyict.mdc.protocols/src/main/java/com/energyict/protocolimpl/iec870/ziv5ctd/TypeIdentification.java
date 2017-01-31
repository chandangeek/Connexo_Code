/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TypeIdentification.java
 *
 * Created on 11 January 2006, 09:11
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

/** @author  fbo */

public class TypeIdentification implements Marshalable {

    private int id;
    private String mnemonic;
    private TypeParser typeParser;

    /** Creates a new instance of TypeIdentification */
    TypeIdentification(int id, String mnemonic, TypeParser typeParser ) {
        this.id = id;
        this.mnemonic = mnemonic;
        this.typeParser = typeParser;
    }

    public int getId() {
        return id;
    }

    TypeParser getTypeParser( ){
        return typeParser;
    }
    
    public boolean isReadProfileCmd( ){
        return this.mnemonic.startsWith( "C_CI" );
    }
    
    public boolean isReadTarification( ){
        return this.mnemonic.startsWith( "C_TA" );
    }
    
    public boolean isReadHistoricalTarification( ) {
        return this.mnemonic.startsWith( "C_TA_VM_2" );
    }
    
    public boolean isReadEvents( ){
        return this.mnemonic.startsWith( "C_SP_NB_2" );
    }
    
    public ByteArray toByteArray() {
        return new ByteArray().add((byte)id );
    }

    public String toString(){

        String hexId = Integer.toHexString(id);
        hexId = ( hexId.length() > 1 ) ? "0x" + hexId : "0x0" + hexId;

        return new StringBuffer()
        .append( "TypeId. [ id=" + id + " (" + hexId + ")," )
        .append( " mnemo=" + mnemonic + "] " )
        .toString();
    }

}