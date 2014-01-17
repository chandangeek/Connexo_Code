/**
 * UNIFLO1200Parsers.java
 *
 * Created on 8-dec-2008, 15:23:58 by jme
 *
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.parsers;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.modbus.flonidan.uniflo1200.register.UNIFLO1200Registers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author jme
 *
 */
public class UNIFLO1200Parsers {

	private static final int DEBUG 				= 0;
	private static final int DECIMALS 			= -1;
	private TimeZone tz 						= null;

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
	public static final String PARSER_GAS_FORM	= "GAS_FORMULA_Parser"; // convert option byte to used conversion type string (AGA8, ...)
	public static final String PARSER_INTERVAL 	= "INTERVAL_Parser";	// convert option byte to log interval time in seconds
	public static final String PARSER_UINT8_SWP = "UNINT8_SWAP_Parser";
	public static final String PARSER_STR1_SWP 	= "STR1_SWAP_Parser";

    public static final int LENGTH_UINT8		= 1; 		// 1 byte
    public static final int LENGTH_UINT16		= 1; 		// 2 bytes (word)
    public static final int LENGTH_UINT32		= 2; 		// 4 bytes (long)
    public static final int LENGTH_REAL32		= 2; 		// 4 bytes (single)
    public static final int LENGTH_INTREAL		= 4;		// 8 bytes (4 bytes long represent the integer part, 4 bytes float represents the fractional part)
    public static final int LENGTH_TIME			= 3;		// 6 bytes (yymmddhhmmss)
    public static final int LENGTH_STR22		= 11;		// 22 chars
    public static final int LENGTH_OPTION		= 1;		// 1 byte (list index)
    public static final int LENGTH_LOC_PTR		= 1;		// 2 bytes (location pointer)
    public static final int LENGTH_STR29		= 15;		// 29 chars
    public static final int LENGTH_STR1			= 1;		// 1 char
    public static final int LENGTH_UINT160		= 1;		// 2 bytes (word, reverse data storing)
    public static final int LENGTH_UINT320		= 2;		// 4 bytes (long, reverse data storing)
    public static final int LENGTH_REAL320		= 2;		// 4 bytes (single, reverse data storing)
    public static final int LENGTH_STR8			= 4;		// 8 chars
//	public static final int LENGTH_GAS_FORM		= 1; 		// convert option byte to used conversion type string (AGA8, ...)

    public static byte[] buildTimeDate(Calendar cal) {
    	byte[] b = new byte[6];

    	b[0] = (byte) ((cal.get(Calendar.YEAR) - 2000) & 0x000000FF);
    	b[1] = (byte) ((cal.get(Calendar.MONTH) + 1) & 0x000000FF);
    	b[2] = (byte) (cal.get(Calendar.DAY_OF_MONTH) & 0x000000FF);
    	b[3] = (byte) (cal.get(Calendar.HOUR_OF_DAY) & 0x000000FF);
    	b[4] = (byte) (cal.get(Calendar.MINUTE) & 0x000000FF);
    	b[5] = (byte) (cal.get(Calendar.SECOND) & 0x000000FF);

    	return b;
    }

    public UNIFLO1200Parsers(TimeZone timezone) {
    	this.tz = timezone;
    }

	private TimeZone getTZ() {
		return tz;
	}

	public class BigDecimalParser implements Parser {
        public Object val(int[] values, AbstractRegister register) throws IOException {
            BigDecimal bd = null;
            if( values.length == 1 ) {
                bd = new BigDecimal( values[0] );
            } else {
                bd = new BigDecimal( (values[0]<<16)+values[1] );
            }

            if (DECIMALS > -1) bd = bd.setScale(DECIMALS, BigDecimal.ROUND_HALF_UP);
            return bd;
        }
    }

    public class TimeParser implements Parser {
		public Object val(int[] values, AbstractRegister register) throws IOException {
			Calendar cal = ProtocolUtils.getCalendar(getTZ());
			cal.set(Calendar.MILLISECOND, 	0x00000000);
			cal.set(Calendar.SECOND, 		(values[2] & 0x000000FF));
			cal.set(Calendar.MINUTE, 		(values[2] & 0x0000FF00) >> 8);
			cal.set(Calendar.HOUR_OF_DAY, 	(values[1] & 0x000000FF));
			cal.set(Calendar.DAY_OF_MONTH, 	(values[1] & 0x0000FF00) >> 8);
			cal.set(Calendar.MONTH, 		(values[0] & 0x000000FF) - 1);
			cal.set(Calendar.YEAR, 			((values[0] & 0x0000FF00) >> 8) + 2000);
			return cal.getTime();
		}
    }

    public class StringParser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
            String result = "";
        	for (int i = 0; i < values.length; i++) {
				result += (char)((values[i] & 0x0000FF00) >> 8);
				result += (char)(values[i] & 0x000000FF);
			}
        	return result;
        }
    }

    public class STR22Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
            String result = "";
            for (int i = 0; i < 11; i++) {
				result += (char)((values[i] & 0x0000FF00) >> 8);
				result += (char)(values[i] & 0x000000FF);
			}
        	return result;
        }
    }

    public class STR1Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
            return "" + (char)((values[0] & 0x0000FF00) >> 8);
        }
    }

    public class STR1SwappedParser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
            return "" + (char)((values[0] & 0x000000FF));
        }
    }

    public class STR29Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
            String result = "";
            for (int i = 0; i < 15; i++) {
				result += (char)((values[i] & 0x0000FF00) >> 8);
				if (i != 14) result += (char)(values[i] & 0x000000FF);
			}
        	return result;
        }
    }

    public class UINT8Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		new Integer(((values[0] & 0x0000FF00) >> 8));
        	return returnValue;
        }
    }

    public class UINT8SwappedParser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		new Integer((values[0] & 0x000000FF));
        	return returnValue;
        }
    }

    public class UINT16Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		new Integer(
        				((values[0] & 0x0000FF00) >> 8) +
        				((values[0] & 0x000000FF) << 8)
        		);
        	return returnValue;
        }
    }

    public class UINT32Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		new Integer(
        				((values[0] & 0x0000FF00) >> 8) +
        				((values[0] & 0x000000FF) << 8) +
        				((values[1] & 0x0000FF00) << 8) +
        				((values[1] & 0x000000FF) << 24)
        		);
        	return returnValue;
        }
    }

    public class UINT160Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		new Integer(
        				((values[0] & 0x0000FF00) >> 8) +
        				((values[0] & 0x000000FF) << 8)
        		);
        	return returnValue;
        }
    }

    public class UINT320Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
        	Integer returnValue =
        		new Integer(
        				((values[0] & 0x0000FF00) >> 8) +
        				((values[0] & 0x000000FF) << 8) +
        				((values[1] & 0x0000FF00) << 8) +
        				((values[1] & 0x000000FF) << 24)
				);
        	return returnValue;
        }
    }


    public class REAL32Parser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
        	int fractionalPart;
        	BigDecimal returnValue;

        	fractionalPart =
        		((values[0] & 0x0000FF00) >> 8) +
				((values[0] & 0x000000FF) << 8) +
				((values[1] & 0x0000FF00) << 8) +
				((values[1] & 0x000000FF) << 24);

        	returnValue = new BigDecimal(Float.intBitsToFloat(fractionalPart));
        	if (DECIMALS > -1) returnValue = returnValue.setScale(DECIMALS, BigDecimal.ROUND_HALF_UP);

        	return returnValue;
        }
    }

    public class INTREALParser implements Parser {
        public Object val(int[] values, AbstractRegister register) {
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
        	if (DECIMALS > -1) returnValue = returnValue.setScale(DECIMALS, BigDecimal.ROUND_HALF_UP);

        	return returnValue;
        }
    }

    public class DATABLOCKParser implements Parser {
		public Object val(int[] values, AbstractRegister register) throws IOException {
			byte[] b = new byte[values.length * 2];
			for (int i = 0; i < values.length; i++) {
        		b[i*2] = (byte) ((values[i] & 0x0000FF00) >> 8);
        		b[(i*2)+1] = (byte) (values[i] & 0x000000FF);
			}
			return b;
		}
    }

    public class GasFormulaParser implements Parser {
    	public Object val(int[] values, AbstractRegister register) {
    		int returnValue = (values[0] & 0x0000FF00) >> 8;
    		return UNIFLO1200Registers.OPTION_GAS_CALC_FORMULA[returnValue];
    	}
    }

    public class IntervalParser implements Parser {
    	public Object val(int[] values, AbstractRegister register) {
    		int returnValue = (values[0] & 0x000000FF);
    		return new Integer(UNIFLO1200Registers.OPTION_LOG_INTERVAL[returnValue]);
    	}
    }

}
