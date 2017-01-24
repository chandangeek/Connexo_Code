/*
 * RtuPlusBusLogbook.java
 *
 * Created on 24 februari 2003, 21:48
 */

package com.energyict.protocolimpl.rtuplusbus;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

//import com.energyict.rtuplusbus.RtuPlusBusFrames;

/**
 *
 * @author  Stefan Grosjean
 */
public class RtuPlusBusLogbook {


    //
    //  L o g b o o k   M e s s a g e s    //
    private static final int LB_VM1           = 1;
    private static final int LB_POWERFAIL     = 65;
    private static final int LB_WATCHDOG      = 66;
    private static final int LB_PARMETERS     = 67;
    private static final int LB_PARRTU        = 68;
    private static final int LB_PARPID        = 69;
    private static final int LB_ERR_RUNTIME   = 70;
    private static final int LB_PARTOT        = 71;
    private static final int LB_MEM_CLEAR     = 72;
    private static final int LB_ERR_FATAL     = 73;
    private static final int LB_AUX_SUPPLY    = 74;
    private static final int LB_TELE          = 75;
    private static final int LB_SYNC_NOK      = 76;
    private static final int LB_SYNC_OK       = 77;
    private static final int LB_CLOCK         = 78;
    private static final int LB_TOT_OVERFLOW  = 79;
    private static final int LB_ERR_EEPROM    = 80;
    private static final int LB_CPYIO_ERR     = 81;
    private static final int LB_TAR           = 82;
    private static final int LB_FF            = 83;
    private static final int LB_NOFUNC        = 84;
    private static final int LB_AMETERS       = 85;
    private static final int LB_ALARMS        = 86;
    private static final int LB_SYNC          = 87;
    private static final int LB_POWERON       = 88;
    private static final int LB_INTVADJUST    = 89;
    private static final int LB_PIDLOADS      = 90;
    private static final int LB_PIDOUTP       = 91;
    private static final int LB_PIDSETP       = 92;
    private static final int LB_COS_OUTP      = 93;
    private static final int LB_TAROUT        = 94;
    private static final int LB_CLOCKERROR    = 95;
    private static final int LB_MODBUS_COM0   = 96;
    private static final int LB_MODBUS_COM1   = 97;
    private static final int LB_ERR_PTR       = 91; // Pointer store out of bounds
    private static final int LB_ERR_ARR       = 92;  // Array index out of bounds
    private static final int LB_ERR_ST_COR    = 93;  // Stack corrupted
    private static final int LB_ERR_ST_OV     = 94;  // Stack overflow
    private static final int LB_ERR_AUX_ST_OV = 95;  // Aux stack overflow
    private static final int LB_ERR_DOMAIN    = 96;  // Domain error
    private static final int LB_ERR_RANGE     = 97;  // Range error
    private static final int LB_ERR_FLPOINT   = 98;  // Floating point overflow
    private static final int LB_ERR_LDIV0     = 99;  // Long divide by zero
    private static final int LB_ERR_LMOD0     = 100; // Long modulus, modulus zero
    private static final int LB_ERR_SUB_OV    = 101; // Subtraction overflow
    private static final int LB_ERR_INT_DIV0  = 102; // Integer divide by zero
    private static final int LB_ERR_INTERRUPT = 103; // Unexpected interrupt
    private static final int LB_ERR_PROG      = 104; // Execute outside program bounds
    private static final int LB_DEBUG         = 105;

    TimeZone timeZone = null;


    /** Creates a new instance of RtuPlusBusLogbook */
    public RtuPlusBusLogbook( TimeZone aTimeZone )
    { this.timeZone = aTimeZone;
    }


    public void parseLogbook( int[] aiReceivedData, ProfileData aProfileData )
    { int i;
      int liEventID;
      long llTimeOfRecord;
      Calendar calendar = Calendar.getInstance( this.timeZone);

      for( i=0; i < 10 ; i++ ) {
          // Get the Event/Logbook Message
          liEventID  = (aiReceivedData[(1 + i * 10)] & 0xFF) << 8;
          liEventID +=  aiReceivedData[(    i * 10)];
          liEventID = mapEventID( liEventID );

          // Get the Time and Convert it
          llTimeOfRecord = (aiReceivedData[(5 + i * 10) ] & 0xFF) << 24;
          llTimeOfRecord +=(aiReceivedData[(4 + i * 10)] & 0xFF) << 16;
          llTimeOfRecord +=(aiReceivedData[(3 + i * 10)] & 0xFF) << 8;
          llTimeOfRecord +=(aiReceivedData[(2 + i * 10)] & 0xFF);
          calendar.set( 1980, calendar.JANUARY, 1, 0, 0, 0);
          calendar.setTimeInMillis( calendar.getTimeInMillis() + ( llTimeOfRecord * 1000 ) );

          // Add the Event to the ProfileData..
          aProfileData.addEvent( new MeterEvent( new Date(calendar.getTime().getTime()), liEventID ) );

      }
    }


    private int mapEventID(int aiEventID ) {
        switch( aiEventID ) {

            // POWERFAIL AND WATCHDOG
            case  LB_POWERFAIL     : return( MeterEvent.POWERDOWN );
            case  LB_POWERON       : return( MeterEvent.POWERUP );
            case  LB_WATCHDOG      : return( MeterEvent.WATCHDOGRESET );

            // CONFIGURATION CHANGES
            case  LB_PARMETERS     : return( MeterEvent.CONFIGURATIONCHANGE );
            case  LB_PARRTU        : return( MeterEvent.CONFIGURATIONCHANGE );
            case  LB_PARPID        : return( MeterEvent.CONFIGURATIONCHANGE );
            case  LB_PARTOT        : return( MeterEvent.CONFIGURATIONCHANGE );
            case  LB_TELE          : return( MeterEvent.CONFIGURATIONCHANGE );

            // OVERFLOW OF TOTALISER OUTPUT
            case  LB_TOT_OVERFLOW  : return( MeterEvent.REGISTER_OVERFLOW );

            // ALL KINDS OF ..
            case  LB_VM1           : return( MeterEvent.OTHER );
            case  LB_CPYIO_ERR     : return( MeterEvent.OTHER );
            case  LB_TAR           : return( MeterEvent.OTHER );
            case  LB_FF            : return( MeterEvent.OTHER );
            case  LB_NOFUNC        : return( MeterEvent.OTHER );
            case  LB_AMETERS       : return( MeterEvent.OTHER );
            case  LB_ALARMS        : return( MeterEvent.OTHER );

            // CLOCK, SYNCHRO AND INTERVAL BOUNDERIES
            case  LB_SYNC          : return( MeterEvent.SETCLOCK );
            case  LB_SYNC_NOK      : return( MeterEvent.SETCLOCK );
            case  LB_SYNC_OK       : return( MeterEvent.SETCLOCK );
            case  LB_CLOCKERROR    : return( MeterEvent.SETCLOCK );
            case  LB_INTVADJUST    : return( MeterEvent.SETCLOCK);
            case  LB_CLOCK         : return( MeterEvent.SETCLOCK );

            // MEMORY PROBLEMS
            case  LB_ERR_EEPROM    : return( MeterEvent.RAM_MEMORY_ERROR );
            case  LB_MEM_CLEAR     : return( MeterEvent.RAM_MEMORY_ERROR );

            // HARDWARE PROBLEMS ..
            case  LB_AUX_SUPPLY    : return( MeterEvent.OTHER );

            // PID REGULATOR AND OTHER ..
            case  LB_PIDLOADS      : return( MeterEvent.OTHER );
            case  LB_PIDOUTP       : return( MeterEvent.OTHER );
            case  LB_PIDSETP       : return( MeterEvent.OTHER );
            case  LB_COS_OUTP      : return( MeterEvent.OTHER );
            case  LB_TAROUT        : return( MeterEvent.OTHER );

            // COMMUNICATION
            case  LB_MODBUS_COM0   : return( MeterEvent.OTHER );
            case  LB_MODBUS_COM1   : return( MeterEvent.OTHER );

            // RUNTIME ERRORS
            case  LB_ERR_RUNTIME   : return( MeterEvent.PROGRAM_FLOW_ERROR );
            case  LB_ERR_FATAL     : return( MeterEvent.PROGRAM_FLOW_ERROR );
            case  LB_ERR_FLPOINT   : return( MeterEvent.PROGRAM_FLOW_ERROR );  // Floating point overflow
            case  LB_ERR_LDIV0     : return( MeterEvent.PROGRAM_FLOW_ERROR );  // Long divide by zero
            case  LB_ERR_LMOD0     : return( MeterEvent.PROGRAM_FLOW_ERROR ); // Long modulus, modulus zero
            case  LB_ERR_SUB_OV    : return( MeterEvent.PROGRAM_FLOW_ERROR ); // Subtraction overflow
            case  LB_ERR_INT_DIV0  : return( MeterEvent.PROGRAM_FLOW_ERROR ); // Integer divide by zero
            case  LB_ERR_INTERRUPT : return( MeterEvent.PROGRAM_FLOW_ERROR ); // Unexpected interrupt
            case  LB_ERR_PROG      : return( MeterEvent.PROGRAM_FLOW_ERROR ); // Execute outside program bounds
            case  LB_DEBUG         : return( MeterEvent.PROGRAM_FLOW_ERROR );

            default: return(MeterEvent.OTHER);
        }
    }

}
