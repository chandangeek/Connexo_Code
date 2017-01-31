/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import java.util.Date;

public class IonParser {
 
    boolean dbg = false;
       
    IonObject parse( Assembly a ) {
        
        try {
        
            int nibble1 = a.nibbleValue();
            int nibble2 = a.nibbleValue();
            
            IonObject result = null;
            
            switch( nibble1 ) {
                case 0x0:   result = parseChar(a, fixedSizeLength( a, nibble2 ) ); 
                            break; 
                case 0x3:   result = parseFloat(a, fixedSizeLength( a, nibble2 ) ); 
                            break;
                case 0x4:   result = parseSignedInteger(a, fixedSizeLength( a, nibble2 ) ); 
                            break;
                case 0x5:   result = parseTime(a, fixedSizeLength( a, nibble2 ) ); 
                            break;
                case 0x6:   result = parseUnsignedInteger(a, fixedSizeLength( a, nibble2 ) ); 
                            break;
                case 0x7:   result = parseStructure( a, nibble2 ); 
                            break;
                case 0x8:   result = parseStructureArray(a, nibble2 ); 
                            break;
                case 0xf:   result = parseVariableSizeClass( a, nibble2 ); 
                            break;
            }
            
            if( result != null ) {
                String s = result.toString();
                if( s.length() > 300 )
                    s = s.substring( 0, 300 );
                debug( a.toString() + " -> " + s );
            } else
                debug( a.toString() + " -> " + "<null>" );
            
            if( result != null ) {
                a.pop();
                
                return result;
            } else {
                throw new ParseException( "class unknow: " + nibble1, a );
            }
            
        } catch( Throwable pex ) {
            pex.printStackTrace();
            System.out.println( a.toString() + " " + pex.getMessage() );
            return null; // TODO handle this properly
        }
        
    }
    
    // Parsing ion primitives / fixed size //
    //____________________________________ //
    
    int fixedSizeLength( Assembly a, int nibble2 ) {
        if( nibble2 <= 0xc ) 
            return nibble2;
        if( nibble2 == 0xd )
            return a.byteValue();
        if( nibble2 == 0xe )
            return a.unsignedIntValue(4);
        if( nibble2 == 0xf )
            return 0;

        throw new ParseException( "invalid size: " + nibble2, a );        
    }
    
    IonObject parseChar( Assembly a, int length ) {
        IonChar ionChar = new IonChar( (char) a.byteValue() );
        a.push( ionChar );
        return ionChar;
    }
    
    IonObject parseFloat( Assembly a, int length ) {
        if( length != 4 )
            throw new ParseException( "Unvalid float length: " + length , a );
        IonFloat f = new IonFloat( Float.intBitsToFloat( a.unsignedIntValue(4) ) );
        a.push( f );
        return f;
        
    }
    
    IonObject parseSignedInteger( Assembly a, int length ) {
        IonInteger ionInt = new IonInteger( a.intValue(length) );
        a.push( ionInt );
        return ionInt;
    }
    
    IonObject parseTime( Assembly a, int length ) {
        IonObject io = new IonObject( );
        io.setType( IonObject.TIME_TYPE );
        a.push( io );
        
        long l = a.longValue();
        long milli = (long)( ( ( l & 0x00000000ffffffffl ) / 2147 ) / 1000 ); 
        Date aDate = new Date( ((l>>31)*1000) + milli );
        io.setValue( aDate );
        return io;
    }
    
    IonObject parseUnsignedInteger( Assembly a, int length ) {
        IonInteger ionInt = new IonInteger( a.unsignedIntValue( length)  );
        a.push( ionInt );
        return ionInt;
    }

    
    // Parsing structures //
    //___________________ //
    
    IonObject parseStructure( Assembly a, int nibble2 ) {
        
        switch( nibble2 ){
            case 0x0: return parseLogRecord( a );
            case 0x1: return parseAlarm( a );
            case 0x2: return parseEvent( a );
            case 0x4: return parseRange( a );
            case 0x5: return parseList( a );
            case 0x7: return parseException( a );
            case 0x8: return parseWaveform( a );
            case 0xa: return parseDate( a );
            case 0xb: return parseCalendar( a );
            case 0xc: return parseProfile( a );
            case 0xf: return parseList( a );
        }
        
        throw new ParseException( "structure unknown", a );
        
    }
    
    IonObject parseLogRecord( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.LOGRECORD_TYPE );
        a.push( ios );
        
        ios.add( "logPosition", parse(a) );
        ios.add( "timestamp", parse(a) );
        
        IonList list = new IonList();
        ios.add( "logValues", list );
        while( (a.peekByte()&0xff) != 0xf3 )
            list.add( parse(a) );
        IonObject ionO = parse(a);  // end of structure exception
        if( ionO.getValue().equals( IonObject.END_OF_STRUCTURE ) ) {
            String msg = "found " + ionO + ", expected " + IonObject.END_OF_STRUCTURE;
            throw new ParseException( msg, a );
        }
        return ios;
    }
    
    IonObject parseAlarm( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.ALARM_TYPE );
        a.push( ios );
        
        ios.add( "effectHandle", parse(a) );
        ios.add( "transitions", parse(a) );
        ios.add( "priority", parse(a) );
        return ios;        
    }
    
    IonObject parseEvent( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.EVENT_TYPE );
        a.push( ios );
        
        ios.add( "priority", parse(a) );
        ios.add( "eventState", parse(a) );
        ios.add( "causeHandle", parse(a) );
        ios.add( "causeValue", parse(a) );
        ios.add( "effectHandle", parse(a) );
        ios.add( "effectValue", parse(a) );
        //parse(a);
        return ios;
    }
    
    IonObject parseRange( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.RANGE_TYPE );
        a.push( ios );
        
        ios.add( "rangeStart", parse(a) );
        ios.add( "rangeEnd", parse(a) );
        return ios;        
    }

    IonObject parseException( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.EXCEPTION_TYPE );
        a.push( ios );
        
        ios.add( "exceptionCode", parse(a) );
        ios.add( "exceptionValue", parse(a) );
        ios.add( "reason", parse(a) );
        IonObject ionO = parse(a);  // end of structure exception
        if( ionO.getValue().equals( IonObject.END_OF_STRUCTURE ) ) {
            String msg = "found " + ionO + ", expected " + IonObject.END_OF_STRUCTURE;
            throw new ParseException( msg, a );
        }
        return ios;        
    }

    IonObject parseWaveform( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.WAVEFORM_TYPE );
        a.push( ios );

        ios.add( "samplingFrequency", parse(a) );
        ios.add( "offset", parse(a) );
        ios.add( "scale", parse(a) );
        ios.add( "timeOfFirstPoint", parse(a) );
        ios.add( "samplePoints", parse(a) );
        return ios;        
    }

    IonObject parseDate( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.DATE_TYPE );
        a.push( ios );
        
        ios.add( "year", parse(a) );
        ios.add( "month", parse(a) );
        ios.add( "scale", parse(a) );
        ios.add( "timeOfFirstPoint", parse(a) );
        ios.add( "samplePoints", parse(a) );
        return ios;        
    }
    
    IonObject parseCalendar( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.CALENDAR_TYPE );
        a.push( ios );
        
        ios.add( "date", parse(a) );
        ios.add( "listOfDays", parse(a) );
        return ios;        
    }    
    
    IonObject parseProfile( Assembly a ){
        IonStructure ios = new IonStructure( );
        ios.setType( IonObject.PROFILE_TYPE );
        a.push( ios );

        ios.add( "indexTable", parse(a) );
        ios.add( "activityList", parse(a) );

        return ios;        
    }    
    
    // Parsing a list of various types //
    //________________________________ //
    
    IonObject parseList( Assembly a ) {
        a.push( "parseList" );
        IonList list = new IonList( );
        while( a.bytesLeft() > 10 ) { // todo fix this
            list.add( parse( a ) );
        }
        return list;
    }
    
    // Parsing structure array //
    //________________________ //
    
    IonList parseStructureArray( Assembly a, int nibble2 ) {
        IonList list = new IonList( );
        a.push( list );
        while( (a.peekByte()&0xff) != 0xf9 ) {
            list.add( parse(a) );
        }
        return list;
    }
    
    // Parsing variable size class //
    //____________________________ //

    IonObject parseVariableSizeClass( Assembly a, int nibble2 ) {
        
        switch( nibble2 ){
            case 0x1:
                return parseBoolean( a, false );
            case 0x2: 
                return parseBoolean( a, true );
            case 0x3: 
                return parseEos(a);
            case 0x6: 
                return parseProgram(a);
            case 0x9: 
                return parseEosa(a);
            case 0xa: 
                return parseCharArray( a, variableSizeLength(a) );
            case 0xb: 
                return parseBooleanArray( a, variableSizeLength(a) );
            case 0xc: 
                return parseFloatArray( a, variableSizeLength(a) );
            case 0xd: 
                return parseSignedIntegerArray( a, variableSizeLength(a) );
            case 0xe: 
                return parseUnsignedIntegerArray( a, variableSizeLength(a) );
        }
        
        throw new ParseException( "structure unknown", a );
        
    }
    
    class VariableSize {
        long numberElements;
        int byteSize;
    }
    
    /* This methods is using the "messy parsing" technique. 
     * The difficulty of parsing the length is that the size of the
     * length field is not know until during the parsing. */
    VariableSize variableSizeLength(Assembly a) {
        VariableSize rslt = new VariableSize();

        // first step, numberElements
        int nibble2 = a.nibbleValue();
        if( nibble2 <= 0xd ) {  // nrElem = next 4 bits
            rslt.numberElements = nibble2;
        } else
        if( nibble2 == 0xe ) {  // nrElem = next 8 bits
            rslt.numberElements = ( a.nibbleValue() << 4 ) | ( a.nibbleValue() );
        } else 
        if( nibble2 == 0xf ) {  // nrElem = next 32 bis
            rslt.numberElements = 
                ( (long)a.nibbleValue() << 28 ) 
                | ( a.nibbleValue() << 24 )
                | ( a.nibbleValue() << 20 )
                | ( a.nibbleValue() << 16 )
                | ( a.nibbleValue() << 12 )
                | ( a.nibbleValue() << 8 )
                | ( a.nibbleValue() << 4 )
                | ( a.nibbleValue() );
        } else
        throw new ParseException( "invalid nr of elements ", a );

        // next step, byteSize
        int nibble = a.nibbleValue();
        if( nibble <= 0xc ) {
            rslt.byteSize = nibble;
        } else
        if( nibble == 0xd ) {   
            rslt.byteSize = ( a.nibbleValue() << 4 ) | a.nibbleValue();
        } else
        if( nibble == 0xf ) {  // byteSize = next 32 bis, or 8 nibbles
            rslt.numberElements = 
                ( (long)a.nibbleValue() << 28 ) 
                | ( a.nibbleValue() << 24 )
                | ( a.nibbleValue() << 20 )
                | ( a.nibbleValue() << 16 )
                | ( a.nibbleValue() << 12 )
                | ( a.nibbleValue() << 8 )
                | ( a.nibbleValue() << 4 )
                | ( a.nibbleValue() );
        } else
        throw new ParseException( "invalid nr of elements ", a );

        return rslt;
        
    }

    IonObject parseBoolean( Assembly a, boolean b ) {
        IonBoolean ib = new IonBoolean( b );
        a.push( ib );
        return ib;
    }
    
    IonObject parseEos( Assembly a ) {
        a.push( IonObject.END_OF_STRUCTURE );
        return IonObject.END_OF_STRUCTURE;
    }
    
    IonObject parseProgram( Assembly a ) {
        a.push( IonObject.PROGRAM );
        return IonObject.PROGRAM;
    }
    
    IonObject parseEosa( Assembly a ){
        a.push( IonObject.END_OF_STRUCTURE_ARRAY );
        return IonObject.END_OF_STRUCTURE_ARRAY;
    }
    
    IonObject parseCharArray( Assembly a, VariableSize size ){
        StringBuffer rslt = new StringBuffer();
        for( int i = 0; i < size.byteSize-1; i++ )
            rslt.append( (char) a.byteValue() );
        a.byteValue(); // drop '\0'
        IonObject ion = new IonObject( );
        ion.setValue( rslt.toString() );
        a.push( ion );
        return ion;
    }
    
    IonList parseBooleanArray( Assembly a, VariableSize size ){
        a.push( "parseBooleanArray" );
        throw new ParseException( "parseBooleanArray nyi", a );
    }
    
    IonList parseFloatArray( Assembly a, VariableSize size){
        a.push( "parseFloatArray" );
        throw new ParseException( "parseFloatArray nyi", a );
    }
    
    IonList parseSignedIntegerArray( Assembly a, VariableSize size ){
        a.push( "parseSignedIntegerArray" );
        throw new ParseException( "parseSignedIntegerArray nyi", a );
    }
    
    IonObject parseUnsignedIntegerArray( Assembly a, VariableSize size ){
        a.push( "parseUnsignedIntegerArray" );
        IonList ionList = new IonList( );
        int elementSize = (int)(size.byteSize / size.numberElements);
        for( int i = 0; i < size.numberElements; i ++ ) {
            ionList.add( new IonInteger( a.unsignedIntValue(elementSize) ) );
        }
        return (IonObject)ionList;
    }
    
    // Parsing variable size class //
    //____________________________ //
    
    class ParseException extends RuntimeException {

        private static final long serialVersionUID = 1L;
        Assembly assembly;
        
        ParseException( String message, Assembly a ) {
            super( message );
        }
        
        public String toString( ) {
            return getMessage() + " " + assembly;
        }
    }
    
    // Debug stuff                  //
    //____________________________ //
    
    void debug( String msg ) {
        if( dbg && (msg != null) )
            System.out.println( msg );
    }
    
}
