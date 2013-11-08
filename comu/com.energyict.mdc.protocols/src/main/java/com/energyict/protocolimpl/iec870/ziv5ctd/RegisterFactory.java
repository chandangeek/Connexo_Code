/*
 * RegisterFactory.java
 *
 * Created on 21 December 2005, 14:22
 */

package com.energyict.protocolimpl.iec870.ziv5ctd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/** @author  fbo */

public class RegisterFactory {
    
    private Ziv5Ctd ziv;
    private AsduFactory asduFactory;
    
    private InformationObjectC0 infoC0;
    
    private InformationObject87 info87Contract1;
    private InformationObject87 info87Contract2;
    private InformationObject87 info87Contract3;
    
    private InformationObject47 infoObject47;
    
    private InformationObject97 infoObject97Contract1;
    private InformationObject97 infoObject97Contract2;
    private InformationObject97 infoObject97Contract3;
    
    private InformationObject88 infoObject88Contract1;
    private InformationObject88 infoObject88Contract2;
    private InformationObject88 infoObject88Contract3;
    
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory( Ziv5Ctd ziv, AsduFactory asduFactory ) {
        this.ziv = ziv;
        this.asduFactory = asduFactory;
    }
    
    public InformationObjectC0 getInformationObjectC0( ) throws IOException {
        if( infoC0 == null ){
            ApplicationFunction af = new ApplicationFunction( ziv );
            Asdu asdu = asduFactory.createType0xA2();
            asdu.setVariableStructureQualifier(1);
            asdu.put( new byte[] { (byte)0x00, (byte)0xc0 } );
            ArrayList a = (ArrayList)af.read( asdu );
            infoC0 = (InformationObjectC0)a.get(0);
        }
        return infoC0;
    }    
    
    public InformationObject48 get48( ) throws IOException {
        ApplicationFunction af = new ApplicationFunction( ziv );
        Asdu asdu = asduFactory.createType0x67();
        asdu.setVariableStructureQualifier(0);
        asdu.put( new byte[] { (byte)0x00 } );
        ArrayList a = (ArrayList)af.read( asdu );
        return (InformationObject48)a.get(0);
    }
    
    InformationObject87 getInfo87Contract1() throws IOException {
        if( info87Contract1 == null ){
            ApplicationFunction af = new ApplicationFunction( ziv );
            Asdu asdu = asduFactory.createType0x85();
            asdu.setVariableStructureQualifier(0);
            asdu.put( new byte[] {(byte)134 } );
            info87Contract1 = (InformationObject87)af.read( asdu );
        }
        return info87Contract1;
    }
    
    InformationObject87 getInfo87Contract2() throws IOException {
        if( info87Contract2 == null ){
            ApplicationFunction af = new ApplicationFunction( ziv );
            Asdu asdu = asduFactory.createType0x85();
            asdu.setVariableStructureQualifier(0);
            asdu.put( new byte[] {(byte)135 } );
            info87Contract2 = (InformationObject87)af.read( asdu );
        }
        return info87Contract2;
    }
    
    InformationObject87 getInfo87Contract3() throws IOException {
        if( info87Contract3 == null ){
            ApplicationFunction af = new ApplicationFunction( ziv );
            Asdu asdu = asduFactory.createType0x85();
            asdu.setVariableStructureQualifier(0);
            asdu.put( new byte[] {(byte)136 } );
            info87Contract3 = (InformationObject87)af.read( asdu );
        }
        return info87Contract3;
    }
    
    InformationObject87 getInfo87(  int contract ) throws IOException {
        if( contract == 0 ){
            return getInfo87Contract1();
        }
        if( contract == 1 ){
            return getInfo87Contract2();
        }
        if( contract == 2 ){
            return getInfo87Contract3();
        }
        return null;
    }
    
    InformationObject47 getInfoObject47() throws IOException {
        if( this.infoObject47 == null ) {
            ApplicationFunction af = new ApplicationFunction( ziv );
            Asdu asdu = asduFactory.createType0x64();
            ArrayList a = (ArrayList)af.read( asdu );
            infoObject47 = (InformationObject47)a.get(0);
        }
        return infoObject47;
    }
    
    /**  */
    InformationObject97 getInfoObject96Contract1( ) throws IOException {
        if( infoObject97Contract1 == null ) {
            ApplicationFunction af = new ApplicationFunction( ziv );
            Asdu asdu = asduFactory.create0x96(134);
            ArrayList a = (ArrayList)af.read( asdu );
            infoObject97Contract1 = (InformationObject97)a.get(0);
        }
        return infoObject97Contract1;
    }
    
    /** */
    InformationObject97 getInfoObject96Contract2( ) throws IOException {
        if( infoObject97Contract2 == null ) {
            ApplicationFunction af = new ApplicationFunction( ziv );
            Asdu asdu = asduFactory.create0x96(135);
            ArrayList a = (ArrayList)af.read( asdu );
            infoObject97Contract2 = (InformationObject97)a.get(0);
        }
        return infoObject97Contract2;
    }
    
    /** */
    InformationObject97 getInfoObject96Contract3( ) throws IOException {
        if( infoObject97Contract3 == null ) {
            ApplicationFunction af = new ApplicationFunction( ziv );
            Asdu asdu = asduFactory.create0x96(136);
            ArrayList a = (ArrayList)af.read( asdu );
            infoObject97Contract3 = (InformationObject97)a.get(0);
        }
        return infoObject97Contract3;
    }
    
    InformationObject97 getInfoObject96( int contract ) throws IOException {
        if( contract == 0 ){
            return getInfoObject96Contract1();
        }
        if( contract == 1 ){
            return getInfoObject96Contract2();
        }
        if( contract == 2 ){
            return getInfoObject96Contract3();
        }
        return null;
    }
    
    InformationObject88 getInfoObject88( int contract) throws IOException {
        
        // from the beginning of time ... until now
        Calendar c = Calendar.getInstance( ziv.getTimeZone() );
        c.set( 1900, 0, 1 );    
        Date start = c.getTime();
        Date end = new Date( );
        
        if( contract == 0 ) {
            if( infoObject88Contract1 == null ) {
                ApplicationFunction af = new ApplicationFunction( ziv );
                Asdu asdu = asduFactory.create0x086(134, start, end);
                infoObject88Contract1 = (InformationObject88)af.read( asdu );
            }
            return infoObject88Contract1;
        }
        
        if( contract == 1 ) {
            if( infoObject88Contract2 == null ) {
                ApplicationFunction af = new ApplicationFunction( ziv );
                Asdu asdu = asduFactory.create0x086(135, start, end);
                infoObject88Contract2 = (InformationObject88) af.read( asdu );
            }
            return infoObject88Contract2;
        }
        
        if( contract == 2 ) {
            if( infoObject88Contract3 == null ) {
                ApplicationFunction af = new ApplicationFunction( ziv );
                Asdu asdu = asduFactory.create0x086(136, start, end);
                infoObject88Contract3 = (InformationObject88)af.read( asdu ) ;
            }
            return infoObject88Contract3;
        }
        
        return null;
    }
    
    ArrayList getMeterEvents( Date start, Date end ) throws IOException {
        
        ArrayList result = new ArrayList( );
        
        CP40Time s = new CP40Time(ziv.getTimeZone(), start);
        CP40Time e = new CP40Time(ziv.getTimeZone(), end);
        
        ApplicationFunction af = new ApplicationFunction( ziv );
        Asdu asdu = asduFactory.createTypeOx66();
        ByteArray byteArray = new ByteArray( new byte [] { 0x34 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { 0x35 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { 0x36 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        af = new ApplicationFunction( ziv );
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0x80 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0x81 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0x82 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0x83 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        af = new ApplicationFunction( ziv );
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0x84 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0x85 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xc8 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xc9 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xcb } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xcc } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xcd } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xd2 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xd3 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xd4 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xd5 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xd6 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xe1 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xd7 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xd8 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xd9 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xda } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xdb } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xe2 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xdc } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xdd } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xde } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xdf } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xe0 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        asdu = asduFactory.createTypeOx66();
        byteArray = new ByteArray( new byte [] { (byte)0xe3 } );
        asdu.put( byteArray.add( s.toByteArray() ).add( e.toByteArray() ).toByteArray() );
        result.addAll((List)af.read(asdu));
        
        return result;
        
    }
    
}
