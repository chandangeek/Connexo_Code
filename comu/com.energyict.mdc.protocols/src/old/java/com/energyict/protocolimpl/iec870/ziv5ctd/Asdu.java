/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** Application Service Data Unit */

public class Asdu implements Marshalable {

    TypeIdentification typeIdentification;
    CauseOfTransmission cot;
    Address address;
    boolean binary;
    
    boolean variableStructureQualifierSet = true;
    int variableStructureQualifier = 1;
    ArrayList informationObjects;
    ByteArray byteArray;

    Asdu( ){
        informationObjects = new ArrayList();
        byteArray = new ByteArray();
    }

    public TypeIdentification getTypeIdentification() {
        return typeIdentification;
    }

    public Asdu setTypeIdentification(TypeIdentification typeIdentification) {
        this.typeIdentification = typeIdentification;
        return this;
    }

    public CauseOfTransmission getTransmissionCause() {
        return cot;
    }

    public Asdu setTransmissionCause(CauseOfTransmission transmissionCause) {
        this.cot= transmissionCause;
        return this;
    }

    public Address getAddress() {
        return address;
    }

    public Asdu setAddress(Address address) {
        this.address = address;
        return this;
    }
    
    public void setVariableStructureQualifier(int vsq){
        variableStructureQualifierSet = true;
        variableStructureQualifier = vsq;
    }

    public List getInformationObjects() {
        return Collections.unmodifiableList( informationObjects );
    }

    public Asdu add( InformationObject informationObject ){
        binary = false;
        informationObjects.add( informationObject );
        return this;
    }
    
    public Asdu put( byte[] content ) {
        binary = true;
        byteArray.add( content );
        return this;
    }

    public ByteArray toByteArray() {
        if( !variableStructureQualifierSet && informationObjects.size() > 0 )
            variableStructureQualifier = informationObjects.size();
        
        ByteArray result = new ByteArray()
            .add(typeIdentification)
            .add((byte)variableStructureQualifier)
            .add(cot)
            .add(address);

        if( binary ) {
            result.add( byteArray );
        } else {
            Iterator i = informationObjects.iterator();
            while( i.hasNext() ){
                result.add( (Marshalable)i.next() );
            }
        }
        
        return result;
    }

    public String toString(){
        StringBuffer rslt = new StringBuffer();
        rslt
            .append("Asdu [")
            .append(" \n ").append(typeIdentification)
            .append(" \n ").append(cot)
            .append(" ]\n").toString();
        Iterator i = getInformationObjects().iterator();
        while( i.hasNext() )
            rslt.append( i.next() + "\n" );
        return rslt.toString();
    }

}
