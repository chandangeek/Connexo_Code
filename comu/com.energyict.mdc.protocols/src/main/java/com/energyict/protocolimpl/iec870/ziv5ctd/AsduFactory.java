/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.util.Date;
import java.util.TimeZone;

/** */

public class AsduFactory {

    Address address;
    private TypeIdentificationFactory typeIdentificationFactory;

    public AsduFactory( Address address, TypeIdentificationFactory typeIdentificationFactory){
        this.address = address;
        this.typeIdentificationFactory = typeIdentificationFactory;
    }

    Asdu createTypeOx66( ) {
        Asdu asdu = new Asdu();

        asdu.setTypeIdentification(typeIdentificationFactory.get(0x66));
        asdu.setVariableStructureQualifier(0);
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);
        return asdu;
    }

    Asdu createTypeOx7A( ) {
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(122));
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);
        return asdu;
    }

    Asdu createType0x85( ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(133));
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);
        return asdu;
    }

    /** One will be used to read the information of Tarificacion (Memorizados
     * Values) selected by time interval. (src.babelfish)*/
    Asdu create0x086( int contract, Date start, Date end ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(0x86));
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);

        CP40Time s = new CP40Time( TimeZone.getDefault(), start );
        CP40Time e = new CP40Time( TimeZone.getDefault(), end );

        byte [] b = new byte[] { (byte)contract };
        ByteArray body = new ByteArray( b );
        body.add( s.toByteArray() ).add( e.toByteArray() );
        asdu.put( body.toByteArray() );

        return asdu;
    }

    Asdu createType0xA2( ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(162));
        asdu.setTransmissionCause( CauseOfTransmission.REQUESTED );
        asdu.setAddress(address);
        return asdu;
    }

    Asdu createType0xA3( ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(163));
        asdu.setTransmissionCause( CauseOfTransmission.REQUESTED );
        asdu.setAddress(address);
        return asdu;
    }

    Asdu createType0xB5( CP56Time time ) {
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(0xb5));
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);
        asdu.put( new ByteArray( new byte [] { 0x00 } ).add(time.toByteArray()).toByteArray() );
        return asdu;
    }

    Asdu createType0xB7( int accessPassword ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(0xb7));
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);
        asdu.put( new byte[]{ 0x00, (byte)accessPassword, 0x00, 0x00, 0x00 } );
        return asdu;
    }

    Asdu createType0x64( ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(0x64));
        asdu.setTransmissionCause( CauseOfTransmission.REQUESTED );
        asdu.setAddress(address);
        asdu.setVariableStructureQualifier(0);
        asdu.put( new byte[]{ 0x00 } );
        return asdu;
    }

    Asdu createType0x67( ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(0x67));
        asdu.setTransmissionCause( CauseOfTransmission.REQUESTED );
        asdu.setAddress(address);
        asdu.setVariableStructureQualifier(0);
        return asdu;
    }

    Asdu create0x7a( int registerAddress, Date start, Date end ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(0x7a));
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);

        CP40Time s = new CP40Time( TimeZone.getDefault(), start );
        CP40Time e = new CP40Time( TimeZone.getDefault(), end );

        ByteArray body =
            new ByteArray( new byte[] { (byte)registerAddress, 0x01, 0x08 } );
        body.add( s.toByteArray() ).add( e.toByteArray() );
        asdu.put( body.toByteArray() );

        return asdu;
    }

    Asdu create0x7b( int registerAddress, Date start, Date end ){
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(0x7b));
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);

        CP40Time s = new CP40Time( TimeZone.getDefault(), start );
        CP40Time e = new CP40Time( TimeZone.getDefault(), end );

        ByteArray body =
            new ByteArray( new byte[] { (byte)registerAddress, 0x01, 0x08 } );
        body.add( s.toByteArray() ).add( e.toByteArray() );
        asdu.put( body.toByteArray() );

        return asdu;
    }

    Asdu create0x96( int register ) {
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentificationFactory.get(0x96));
        asdu.setTransmissionCause( CauseOfTransmission.REQUESTED );
        asdu.setAddress(address);
        asdu.put( new byte[]{ (byte)register, (byte)193 } );
        return asdu;
    }

    Asdu get(TypeIdentification typeIdentification) {
        Asdu asdu = new Asdu();
        asdu.setTypeIdentification(typeIdentification);
        asdu.setTransmissionCause( CauseOfTransmission.ACTIVATION );
        asdu.setAddress(address);
        return asdu;
    }

    Asdu get( int id ){
        TypeIdentification ti = typeIdentificationFactory.get(id);
        return get(ti);
    }

    Asdu parse( ByteArray byteArray ){

        Asdu asdu = new Asdu();

        asdu.setTypeIdentification(
            typeIdentificationFactory.get(byteArray.intValue(0)));
        asdu.setTransmissionCause(
            CauseOfTransmission.get(byteArray.intValue(2)));

        if(asdu.getTypeIdentification().getTypeParser()!=null) {
            TypeParser tp = asdu.getTypeIdentification().getTypeParser();
            asdu.add(tp.parse( byteArray ) );
        }

        return asdu;

    }

}
