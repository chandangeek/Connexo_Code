package com.energyict.genericprotocolimpl.common;

import java.text.MessageFormat;

/**
 * Class containing all messages as constants.  These can be errors, warnings,
 * etc.
 * 
 * @author fbo
 */

public class Messages {

    public static MessageFormat NO_DEVICE_FOUND = 
        toMf( "No Device found with serial: \"{0}\"" );
    
    public static MessageFormat TOO_MANY_DEVICES_FOUND =
        toMf( "{0} devices found with serial: \"{1}\".  " +
        	  "Only 1 Device is allowed." );
    
    /* short hand notation */
    static MessageFormat toMf(String format) {
        return new MessageFormat( format );
    }
    
    public static void main( String [] args ){
        
        System.out.println( 
            NO_DEVICE_FOUND.format(new Object[] { "1234567" } ) );

        System.out.println( 
            TOO_MANY_DEVICES_FOUND.format(
                new Object[] { new Integer( 5 ), "1234567" } ) );

        
    }
    
}
