/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

class IonObject {
    
    final static String TIME_TYPE = "Time";
    final static String LOGRECORD_TYPE = "LogRecord";
    final static String ALARM_TYPE = "Alarm";
    final static String EVENT_TYPE = "Event";
    final static String RANGE_TYPE = "Range";
    final static String EXCEPTION_TYPE = "Exception";
    final static String WAVEFORM_TYPE = "Waveform";
    final static String DATE_TYPE = "Date";
    final static String CALENDAR_TYPE = "Calendar";
    final static String PROFILE_TYPE = "Profile"; 
    
    final static IonObject END_OF_STRUCTURE = 
        new IonObject( ).setValue( "End of structure" );
    final static IonObject PROGRAM = 
        new IonObject( ).setValue( "Program" );
    final static IonObject END_OF_STRUCTURE_ARRAY = 
        new IonObject( ).setValue( "End of structure array" ); 
    
    String type;
    Object value;

    IonObject setType( String type ) {
        this.type = type;
        return this;
    }
    
    IonObject setValue(Object value) {
        this.value = value;
        return this;
    }

    Object getValue() {
        return value;
    }

    boolean isFloat( ) {
        return false;
    }
    
    boolean isInteger( ){
        return false;
    }
    
    boolean isException( ){
        return type != null && EXCEPTION_TYPE.equals( type );
    }
    
    boolean isTime( ){
        return type != null && TIME_TYPE.equals( type );
    }
    
    boolean isEndOf( ) {
        if( this == END_OF_STRUCTURE ) return true;
        if( this == END_OF_STRUCTURE_ARRAY ) return true;
        return false;
    }
    
    boolean isStrucure() {
        if( TIME_TYPE.equals( type ) )      return true;
        if( LOGRECORD_TYPE.equals( type ) ) return true;
        if( ALARM_TYPE.equals( type ) )     return true;
        if( EVENT_TYPE.equals( type ) )     return true;
        if( RANGE_TYPE.equals( type ) )     return true;
        if( EXCEPTION_TYPE.equals( type ) ) return true;
        if( WAVEFORM_TYPE.equals( type ) )  return true;
        if( DATE_TYPE.equals( type ) )      return true;
        if( CALENDAR_TYPE.equals( type ) )  return true;
        if( PROFILE_TYPE.equals( type ) )   return true;
        return false;
    }
    
    public String toString() {
        return type + "[ " + value.toString() + " ]"; 
    }
    
}