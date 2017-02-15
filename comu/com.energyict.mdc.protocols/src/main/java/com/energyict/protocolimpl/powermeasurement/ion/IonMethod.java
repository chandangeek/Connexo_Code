/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

public class IonMethod {

    final static IonMethod READ_ION_CLASS = 
        create( new byte[] { 0x01 } )
        .setDescription( "Read ION class");
    
    final static IonMethod READ_ION_NAME = 
        create( new byte[] { 0x02 } )
        .setDescription( "Read ION name");
    
    final static IonMethod READ_ION_LABEL = 
        create( new byte[] { 0x03} )
        .setDescription( "Read ION label");

    final static IonMethod READ_REGISTER_TIME = 
        create( new byte[] { 0x14} )
        .setDescription( "Read register time");
    
    final static IonMethod READ_REGISTER_VALUE = 
        create( new byte[] { 0x15} )
        .setDescription( "Read register value");
    
    final static IonMethod WRITE_REGISTER_TIME = 
        create( new byte[] { 0x16} )
        .setDescription( "Write register value");

    final static IonMethod READ_BOOLEAN_REGISTER_ON_NAME = 
        create( new byte[] { 0x1c} )
        .setDescription( "Read boolean register on name");
    
    final static IonMethod READ_BOOLEAN_REGISTER_OFF_NAME = 
        create( new byte[] { 0x1d} )
        .setDescription( "Read boolean register off name");
    
    final static IonMethod READ_BOOLEAN_REGISTER_ON_LABEL = 
        create( new byte[] { 0x1e} )
        .setDescription( "Read boolean register on label");
    
    final static IonMethod READ_BOOLEAN_REGISTER_OFF_LABEL = 
        create( new byte[] { 0x1f} )
        .setDescription( "Read boolean register off label");
    
    final static IonMethod READ_BOOLEAN_REGISTER_STATE_LABEL = 
        create( new byte[] { 0x20} )
        .setDescription( "Read boolean register state label");

    final static IonMethod READ_ARRAY_REGISTER_DEPTH = 
        create( new byte[] { 0x23} )
        .setDescription( "Read array register depth");
    
    final static IonMethod READ_ARRAY_REGISTER_ROLLOVER = 
        create( new byte[] { 0x25} )
        .setDescription( "Read array register rollover");

    final static IonMethod READ_LOG_REGISTER_POSITION = 
        create( new byte[] { 0x28} )
        .setDescription( "Read log register position");

    final static IonMethod READ_EVENT_LOG_ALARMS = 
        create( new byte[] { 0x2d} )
        .setDescription( "Read event log alarms");

    final static IonMethod READ_MODULE_SETUP_COUNTER = 
        create( new byte[] { 0x50} )
        .setDescription( "Read module setup counter");

    final static IonMethod READ_MANAGER_SETUP_COUNTER = 
        create( new byte[] { 0x64} )
        .setDescription( "Read manager setup counter");

    final static IonMethod READ_VALUE = 
        create( new byte[] { (byte)0x95} )
        .setDescription( "Read value");

    final static IonMethod WRITE_VALUE = 
        create( new byte[] { (byte)0x96} )
        .setDescription( "Write value");
  
    final static IonMethod WRITE_ION_LABEL = 
        create( new byte [] { (byte)0xff, (byte)0x00, (byte)0x80 } )
            .setDescription( "Write ION label" );
    
    final static IonMethod READ_SECURITY_LEVEL = 
        create( new byte [] { (byte)0xff, (byte)0x00, (byte)0x81} )
        .setDescription( "Read Security level" );
    
    final static IonMethod READ_ALL_SECURITY_LEVELS = 
        create( new byte [] { (byte)0xff, (byte)0x00, (byte)0x82} )
        .setDescription( "Read All Security levels" );

    final static IonMethod READ_PARENT_HANDLE = 
        create( new byte [] { (byte)0xff, (byte)0x00, (byte)0x83} )
        .setDescription( "Read Parent handle" );

    final static IonMethod READ_OWNERS = 
        create( new byte [] { (byte)0xff, (byte)0x00, (byte)0x84} )
        .setDescription( "Read Owners" );

    final static IonMethod READ_ISA = 
        create( new byte [] { (byte)0xff, (byte)0x00, (byte)0x85} )
        .setDescription( "Read ISA" );

    final static IonMethod WRITE_BOOLEAN_REGISTER_ON_LABEL = 
        create( new byte [] { (byte)0xff, (byte)0x01, (byte)0xf4} )
        .setDescription( "Write Boolean register on label" );

    final static IonMethod WRITE_BOOLEAN_REGISTER_OFF_LABEL = 
        create( new byte [] { (byte)0xff, (byte)0x01, (byte)0xf5} )
        .setDescription( "Write Boolean register off label" );

    final static IonMethod READ_ENUMERATIONS = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x08} )
        .setDescription( "Read Enumerations" );

    final static IonMethod READ_NUMERIC_BOUNDS = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x1c} )
        .setDescription( "Read Numeric bounds" );

    final static IonMethod READ_EVENT_LOG_ALARM_ROLLOVER = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x30} )
        .setDescription( "Read Event log alarm rollover" );

    final static IonMethod READ_CALENDAR_VALUE = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x59} )
        .setDescription( "Read Calendar value" );

    final static IonMethod WRITE_CALENDAR_VALUE = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x5a} )
        .setDescription( "Write Calendar value" );

    final static IonMethod READ_CALENDAR_PROFILE = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x5b} )
        .setDescription( "Read Calendar profile" );

    final static IonMethod WRITE_CALENDAR_PROFILE = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x5c} )
        .setDescription( "Write Calendar profile" );

    final static IonMethod READ_PROFILE_LABELS = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x5d} )
        .setDescription( "Read Profile labels" );

    final static IonMethod WRITE_PROFILE_LABELS = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x5e} )
        .setDescription( "Write Profile labels" );

    final static IonMethod READ_PROFILE_NAMES = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x5f} )
        .setDescription( "Read Profile names" );

    final static IonMethod WRITE_ACTIVITY_DEPTH = 
        create( new byte [] { (byte)0xff, (byte)0x02, (byte)0x60} )
        .setDescription( "Write Activity depth" );

    final static IonMethod READ_MODULE_INPUT_HANDLES = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xe8} )
        .setDescription( "Read Module input handles" );

    final static IonMethod WRITE_MODULE_INPUT_HANDLES = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xe9} )
        .setDescription( "Write Module input handles" );

    final static IonMethod READ_MODULE_INPUT_CLASSES = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xea} )
        .setDescription( "Read Module input classes" );

    final static IonMethod READ_MODULE_OUTPUT_HANDLES = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xeb} )
        .setDescription( "Read Module output handles" );

    final static IonMethod READ_MODULE_SETUP_HANDLES = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xec} )
        .setDescription( "Read Module setup handles" );

    final static IonMethod READ_MODULE_UPDATE_PERIOD = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xed} )
        .setDescription( "Read Module update period" );

    final static IonMethod READ_MODULE_SECURITY = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xee} )
        .setDescription( "Read Module security" );

    final static IonMethod READ_MODULE_INPUT_NAMES = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xef} )
        .setDescription( "Read Module input names" );

    final static IonMethod WRITE_MODULE_SETUP_COUNTER = 
        create( new byte [] { (byte)0xff, (byte)0x03, (byte)0xfc} )
        .setDescription( "Write Module setup counter" );

    final static IonMethod READ_MODULE_STATE = 
        create( new byte [] { (byte)0xff, (byte)0x04, (byte)0x06} )
        .setDescription( "Read Module state" );
    
    final static IonMethod WRITE_MODULE_STATE = 
        create( new byte [] { (byte)0xff, (byte)0x04, (byte)0x07} )
        .setDescription( "Write Module state" );
    
    final static IonMethod WRITE_RECORDER_MODULE_REARM = 
        create( new byte [] { (byte)0xff, (byte)0x04, (byte)0x08} )
        .setDescription( "Write Recorder Module rearm" );
    
    final static IonMethod CREATE_MODULE = 
        create( new byte [] { (byte)0xff, (byte)0x05, (byte)0xdc} )
        .setDescription( "Create module" );
    
    final static IonMethod DESTROY_MODULE = 
        create( new byte [] { (byte)0xff, (byte)0x05, (byte)0xdd} )
        .setDescription( "Destroy module" );
    
    final static IonMethod READ_MANAGED_CLASS = 
        create( new byte [] { (byte)0xff, (byte)0x05, (byte)0xde} )
        .setDescription( "Read Managed class" );
    
    final static IonMethod WRITE_MANAGER_SETUP_COUNTER = 
        create( new byte [] { (byte)0xff, (byte)0x05, (byte)0xdf} )
        .setDescription( "Write Manager setup counter" );
    
    final static IonMethod READ_COLLECTIVE_STATE = 
        create( new byte [] { (byte)0xff, (byte)0x05, (byte)0xe1} )
        .setDescription( "Read collective state" );
    

    ByteArray id;
    String description;

    private static IonMethod create( byte [] id ) {
        return new IonMethod( id );
    }
    
    private IonMethod(byte [] id ) {
        this.id = new ByteArray( id );
    }
    
    private IonMethod setDescription( String description ){
        this.description = description;
        return this;
    }

    ByteArray toByteArray( ) {
        return id;
    }
    
    public String toString() {
        return "IonMethod [" + id.toHexaString(true) + ", " + description + " ]";
    }
    
}
