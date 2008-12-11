/**
 * UNIFLO1200Parsers.java
 * 
 * Created on 8-dec-2008, 15:23:58 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.Calendar;
import java.util.TimeZone;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;

/**
 * @author jme
 *
 */
public class UNIFLO1200Parsers {
	
	private static final int DEBUG = 1;
	private TimeZone tz = null;
	
    public static final String PARSER_UINT8		= "UINT8_Parser"; 		// 1 byte
    public static final String PARSER_UINT16	= "UINT16_Parser"; 		// 2 bytes (word)
    public static final String PARSER_UINT32	= "UINT32_Parser"; 		// 4 bytes (long)
    public static final String PARSER_REAL32	= "REAL32_Parser"; 		// 4 bytes (single)
    public static final String PARSER_INTREAL	= "INTREAL_Parser";		// 8 bytes (4 bytes long represent the integer part, 4 bytes float represents the fractional part)
    public static final String PARSER_TIME		= "Date";				// 6 bytes (yymmddhhmmss)
    public static final String PARSER_STR22		= "STR22_Parser";		// 22 chars
    public static final String PARSER_OPTION	= "OPTION_Parser";		// 1 byte (list index)
    public static final String PARSER_LOC_PTR	= "LOC_PTR_Parser";		// 2 bytes (location pointer)
    public static final String PARSER_STR29		= "STR29_Parser";		// 29 chars
    public static final String PARSER_STR1		= "STR1_Parser";		// 1 char
    public static final String PARSER_UINT160	= "UINT160_Parser";		// 2 bytes (word, reverse data storing) 
    public static final String PARSER_UINT320	= "UINT320_Parser";		// 4 bytes (long, reverse data storing) 
    public static final String PARSER_REAL320	= "REAL320_Parser";		// 4 bytes (single, reverse data storing) 
    public static final String PARSER_STR8		= "STR8_Parser";		// 8 chars
    public static final String PARSER_DATABLOCK	= "DATABLOCK_Parser";	// x bytes (Data block of bytes. Length is unknown)
    public static final String PARSER_STRING 	= "String_Parser";		// x chars (Data block of chars, Length is unknown)
    
    public static final int LENGTH_UINT8		= 1; 		// 1 byte
    public static final int LENGTH_UINT16		= 1; 		// 2 bytes (word)
    public static final int LENGTH_UINT32		= 2; 		// 4 bytes (long)
    public static final int LENGTH_REAL32		= 2; 		// 4 bytes (single)
    public static final int LENGTH_INTREAL		= 4;		// 8 bytes (4 bytes long represent the integer part, 4 bytes float represents the fractional part)
    public static final int LENGTH_TIME			= 3;		// 6 bytes (yymmddhhmmss)
    public static final int LENGTH_STR22		= 11;		// 22 chars
    public static final int LENGTH_OPTION		= 1;		// 1 byte (list index)
    public static final int LENGTH_LOC_PTR		= 2;		// 2 bytes (location pointer)
    public static final int LENGTH_STR29		= 15;		// 29 chars
    public static final int LENGTH_STR1			= 1;		// 1 char
    public static final int LENGTH_UINT160		= 1;		// 2 bytes (word, reverse data storing) 
    public static final int LENGTH_UINT320		= 2;		// 4 bytes (long, reverse data storing) 
    public static final int LENGTH_REAL320		= 2;		// 4 bytes (single, reverse data storing) 
    public static final int LENGTH_STR8			= 4;		// 8 chars
    
    public UNIFLO1200Parsers(TimeZone timezone) {
    	this.tz = timezone;
    }

	private TimeZone getTZ() {
		return tz;
	}

    class BigDecimalParser implements Parser {
        public BigDecimal val(int[] values, AbstractRegister register) throws IOException {
            BigDecimal bd = null;
            if( values.length == 1 ) {
                bd = new BigDecimal( values[0] );
            } else {
                bd = new BigDecimal( (values[0]<<16)+values[1] );
            }
            
//            bd = bd.movePointRight( getScaleForObis( register.getObisCode() ) );
            bd = bd.setScale(6, BigDecimal.ROUND_HALF_UP);
            return bd;
        }
    }
    
    class TimeParser implements Parser {
		public Object val(int[] values, AbstractRegister register) throws IOException {
			Calendar cal = ProtocolUtils.getCalendar(getTZ());
			cal.set(Calendar.SECOND, 		(values[2] & 0x000000FF));
			cal.set(Calendar.MINUTE, 		(values[2] & 0x0000FF00) >> 8);
			cal.set(Calendar.HOUR_OF_DAY, 	(values[1] & 0x000000FF));
			cal.set(Calendar.DAY_OF_MONTH, 	(values[1] & 0x0000FF00) >> 8);
			cal.set(Calendar.MONTH, 		(values[0] & 0x000000FF) - 1);
			cal.set(Calendar.YEAR, 			((values[0] & 0x0000FF00) >> 8) + 2000);
			return cal.getTime();
		}
    }
    
    class StringParser implements Parser {
        public String val(int[] values, AbstractRegister register) {
            String result = "";
        	for (int i = 0; i < values.length; i++) {
				result += (char)((values[i] & 0x0000FF00) >> 8); 
				result += (char)(values[i] & 0x000000FF); 
			}
        	return result;
        }
    }

    class STR22Parser implements Parser {
        public String val(int[] values, AbstractRegister register) {
            String result = "";
            for (int i = 0; i < LENGTH_STR22; i++) {
				result += (char)((values[i] & 0x0000FF00) >> 8); 
				result += (char)(values[i] & 0x000000FF); 
			}
        	return result;
        }
    }

    class STR29Parser implements Parser {
        public String val(int[] values, AbstractRegister register) {
            String result = "";
            for (int i = 0; i < LENGTH_STR29; i++) {
				result += (char)((values[i] & 0x0000FF00) >> 8); 
				result += (char)(values[i] & 0x000000FF); 
			}
        	return result;
        }
    }
    
    class UINT8Parser implements Parser {
        public Integer val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		((values[0] & 0x0000FF00) >> 8);
        	return returnValue;
        }
    }

    class UINT16Parser implements Parser {
        public Integer val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		((values[0] & 0x0000FF00) >> 8) +
        		((values[0] & 0x000000FF) << 8);
        	return returnValue;
        }
    }

    class UINT32Parser implements Parser {
        public Integer val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		((values[0] & 0x0000FF00) >> 8) +
				((values[0] & 0x000000FF) << 8) +
				((values[1] & 0x0000FF00) << 8) +
				((values[1] & 0x000000FF) << 24);
        	return returnValue;
        }
    }

    class REAL32Parser implements Parser {
        public BigDecimal val(int[] values, AbstractRegister register) {
        	int fractionalPart;
        	BigDecimal returnValue;

        	fractionalPart = 
        		((values[0] & 0x0000FF00) >> 8) +
				((values[0] & 0x000000FF) << 8) +
				((values[1] & 0x0000FF00) << 8) +
				((values[1] & 0x000000FF) << 24);

        	returnValue = new BigDecimal(Float.intBitsToFloat(fractionalPart));
        	
        	return returnValue;
        }
    }

    class INTREALParser implements Parser {
        public BigDecimal val(int[] values, AbstractRegister register) {
        	BigDecimal returnValue;
        	int intPart;
        	int fractionalPart;
        	
        	intPart = 
        		((values[0] & 0x0000FF00) >> 8) +
				((values[0] & 0x000000FF) << 8) +
				((values[1] & 0x0000FF00) << 8) +
				((values[1] & 0x000000FF) << 24);

        	fractionalPart = 
        		((values[2] & 0x0000FF00) >> 8) +
				((values[2] & 0x000000FF) << 8) +
				((values[3] & 0x0000FF00) << 8) +
				((values[3] & 0x000000FF) << 24);
        	
        	returnValue = new BigDecimal(intPart);
        	returnValue = returnValue.add(new BigDecimal(Float.intBitsToFloat(fractionalPart)));

        	if (DEBUG >= 1) System.out.print("\n\n");
        	if (DEBUG >= 1) System.out.println("values[0] = " + ProtocolUtils.buildStringHex(values[0] & 0x0000FFFF, 4));
        	if (DEBUG >= 1) System.out.println("values[1] = " + ProtocolUtils.buildStringHex(values[1] & 0x0000FFFF, 4));
        	if (DEBUG >= 1) System.out.println("values[2] = " + ProtocolUtils.buildStringHex(values[2] & 0x0000FFFF, 4));
        	if (DEBUG >= 1) System.out.println("values[3] = " + ProtocolUtils.buildStringHex(values[3] & 0x0000FFFF, 4));
        	if (DEBUG >= 1) System.out.println("");
        	if (DEBUG >= 1) System.out.println("INTREALParser intPart:        " + intPart);
        	if (DEBUG >= 1) System.out.println("INTREALParser fractionalPart: " + fractionalPart);
        	if (DEBUG >= 1) System.out.println("INTREALParser intBitsToFloat: " + Float.intBitsToFloat(fractionalPart));
        	if (DEBUG >= 1) System.out.println("INTREALParser returnValue:    " + returnValue);
        	if (DEBUG >= 1) System.out.print("\n\n");
        	
        	return returnValue;
        }
    }

}
