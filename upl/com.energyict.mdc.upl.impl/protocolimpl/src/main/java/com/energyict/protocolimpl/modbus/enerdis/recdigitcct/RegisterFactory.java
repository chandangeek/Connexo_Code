/*
 * RegisterFactory.java
 *
 * Created on 30 maart 2007, 17:30
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.enerdis.recdigitcct;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
import com.energyict.protocolimpl.modbus.core.AbstractRegisterFactory;
import com.energyict.protocolimpl.modbus.core.HoldingRegister;
import com.energyict.protocolimpl.modbus.core.Modbus;
import com.energyict.protocolimpl.modbus.core.Parser;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/** 
 * Responsibilities:
 *  - Accesspoint to Registers 
 *  - All parsing 
 * 
 * @author fbo
 * @beginchanges
 * GNA|25042008| changed default timeZone to meterTimezone 
 */

class RegisterFactory extends AbstractRegisterFactory {
    
    private final static boolean debug = false;

    private HoldingRegister entry_1;
    private HoldingRegister entry_2;
    private HoldingRegister entry_3;
    private HoldingRegister entry_4;
    private HoldingRegister entry_5;
    private HoldingRegister entry_6;
    private HoldingRegister entry_7;
    private HoldingRegister entry_8;
    
    private HoldingRegister [] entry_1_monthly;
    private HoldingRegister [] entry_2_monthly;
    private HoldingRegister [] entry_3_monthly;
    private HoldingRegister [] entry_4_monthly;
    private HoldingRegister [] entry_5_monthly;
    private HoldingRegister [] entry_6_monthly;
    private HoldingRegister [] entry_7_monthly;
    private HoldingRegister [] entry_8_monthly;
    

    private String monthMap[];
    
    private static String mothName [] = new String[] {
        "January",  "February", "March",
        "April",    "May",      "June",
        "July",     "August",   "September",
        "October",  "November", "December"
    };
    
    
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory(Modbus modBus) {
        super(modBus);
    }
    
    protected void init() {

        String d;
        
        d = "Total entry 1 ";
        entry_1 = add( "1.1.82.8.0.255", 0x0004, d);
        d = "Total entry 2 ";
        entry_2 = add( "1.2.82.8.0.255", 0x0006, d);
        d = "Total entry 3 ";
        entry_3 = add( "1.3.82.8.0.255", 0x0008, d);
        d = "Total entry 4 ";
        entry_4 = add( "1.4.82.8.0.255", 0x000A, d);
        d = "Total entry 5 ";
        entry_5 = add( "1.5.82.8.0.255", 0x000C, d);
        d = "Total entry 6 ";
        entry_6 = add( "1.6.82.8.0.255", 0x000E, d);
        d = "Total entry 7 ";
        entry_7 = add( "1.7.82.8.0.255", 0x0010, d);
        d = "Total entry 8 ";
        entry_8 = add( "1.8.82.8.0.255", 0x0012, d);
        

        Calendar cNow = Calendar.getInstance( getModBus().gettimeZone() );
        int month = cNow.get(Calendar.MONTH);
        this.monthMap = new String[12]; 
        
        /* current month */
        monthMap[month]     = "255";
        
        /* the other 11 */
        int vz = 10;
        for( int idx = 1; idx < 12; idx ++ ) {
            
            if( vz == 0 ) {
				monthMap[(month + idx) % 12] = "VZ";
			} else {
				monthMap[(month + idx) % 12] = "VZ-" + vz;
			}
            
            vz = vz - 1;
            
        }

        entry_1_monthly = new HoldingRegister [12]; 
        int startAddress = 0x0014;
        String dPrefix = "Entry 1 ";

        for( int i = 0; i < 12; i ++ ){
            
            String obis = "1.1.82.8.0." + monthMap[i];
            int address = startAddress + (i*2);
            String description = dPrefix + mothName[i];
            
            entry_1_monthly[i] = add( obis, address, description );
            
        }
        
        entry_2_monthly = new HoldingRegister [12]; 
        startAddress = 0x002C;
        dPrefix = "Entry 2 ";

        for( int i = 0; i < 12; i ++ ){
            
            String obis = "1.2.82.8.0." + monthMap[i];
            int address = startAddress + (i*2);
            String description = dPrefix + mothName[i];
            
            entry_2_monthly[i] = add( obis, address, description );
            
        }

        entry_3_monthly = new HoldingRegister [12]; 
        startAddress = 0x0044;
        dPrefix = "Entry 3 ";

        for( int i = 0; i < 12; i ++ ){
            
            String obis = "1.3.82.8.0." + monthMap[i];
            int address = startAddress + (i*2);
            String description = dPrefix + mothName[i];
            
            entry_3_monthly[i] = add( obis, address, description );
            
        }
        
        entry_4_monthly = new HoldingRegister [12]; 
        startAddress = 0x005C;
        dPrefix = "Entry 4 ";

        for( int i = 0; i < 12; i ++ ){
            
            String obis = "1.4.82.8.0." + monthMap[i];
            int address = startAddress + (i*2);
            String description = dPrefix + mothName[i];
            
            entry_4_monthly[i] = add( obis, address, description );
            
        }
        
        entry_5_monthly = new HoldingRegister [12]; 
        startAddress = 0x0074;
        dPrefix = "Entry 5 ";

        for( int i = 0; i < 12; i ++ ){
            
            String obis = "1.5.82.8.0." + monthMap[i];
            int address = startAddress + (i*2);
            String description = dPrefix + mothName[i];
            
            entry_5_monthly[i] = add( obis, address, description );
            
        }
        
        entry_6_monthly = new HoldingRegister [12]; 
        startAddress = 0x008C;
        dPrefix = "Entry 6 ";

        for( int i = 0; i < 12; i ++ ){
            
            String obis = "1.6.82.8.0." + monthMap[i];
            int address = startAddress + (i*2);
            String description = dPrefix + mothName[i];
            
            entry_6_monthly[i] = add( obis, address, description );
            
        }
        
        entry_7_monthly = new HoldingRegister [12]; 
        startAddress = 0x00A4;
        dPrefix = "Entry 7 ";

        for( int i = 0; i < 12; i ++ ){
            
            String obis = "1.7.82.8.0." + monthMap[i];
            int address = startAddress + (i*2);
            String description = dPrefix + mothName[i];
            
            entry_7_monthly[i] = add( obis, address, description );
            
        }
        
        entry_8_monthly = new HoldingRegister [12]; 
        startAddress = 0x00BC;
        dPrefix = "Entry 8 ";

        for( int i = 0; i < 12; i ++ ){
            
            String obis = "1.8.82.8.0." + monthMap[i];
            int address = startAddress + (i*2);
            String description = dPrefix + mothName[i];
            
            entry_8_monthly[i] = add( obis, address, description );
            
        }
        
    }
    
    /** create a HoldingRegister (=factory method) */
    private HoldingRegister add(String obis, int address, String description){
        
        ObisCode oc = ObisCode.fromString(obis);
        Type type = Type.UNSIGNED_LONG;
        int wordSize = type.wordSize();
        
        Unit unit = Unit.get( BaseUnit.UNITLESS );
        
        HoldingRegister hr = 
            new HoldingRegister(address, wordSize, oc, unit, description);
        hr.setRegisterFactory(this);
        hr.setParser(type.toString());
        
        getRegisters().add( hr );

        return hr;
        
    }
    
    protected void initParsers() {
        
        
        getParserFactory().addParser(Type.UNSIGNED_LONG.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) 
                throws IOException {
                
                dbg( "Parser.val( " + Type.UNSIGNED_LONG + " )" );
                BigDecimal bd = toBigDecimal(Type.UNSIGNED_LONG, values);
                bd = scale( bd );
                Quantity q = new Quantity( bd, register.getUnit() );
                return new RegisterValue( register.getObisCode(), q );
            }
        });

    } 
    
    
    Date toDate(int[] values) {
        
        Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
        int bcd[] = new int[16];
        
        bcd[0] = (values[0]&0x0000f000)>>12;
        bcd[1] = (values[0]&0x00000f00)>>8;
        bcd[2] = (values[0]&0x000000f0)>>4;
        bcd[3] = (values[0]&0x0000000f);
        
        bcd[4] = (values[1]&0x0000f000)>>12;
        bcd[5] = (values[1]&0x00000f00)>>8;
        bcd[6] = (values[1]&0x000000f0)>>4;
        bcd[7] = (values[1]&0x0000000f);
        
        bcd[8] = (values[2]&0x0000f000)>>12;
        bcd[9] = (values[2]&0x00000f00)>>8;
        bcd[10]= (values[2]&0x000000f0)>>4;
        bcd[11]= (values[2]&0x0000000f);

        bcd[12] = (values[3]&0x0000f000)>>12;
        bcd[13] = (values[3]&0x00000f00)>>8;
        bcd[14] = (values[3]&0x000000f0)>>4;
        bcd[15] = (values[3]&0x0000000f);
        
        int year = (bcd[2]*10) + bcd[3];
        if( year > 60 ) {
			year += 1900;
		} else {
			year += 2000;
		}
        
        cal.set(Calendar.MONTH,         ((bcd[0]*10) + bcd[1]) -1);
        cal.set(Calendar.YEAR,          year);
        cal.set(Calendar.DAY_OF_MONTH,  (bcd[6]*10)  + bcd[7]);
        cal.set(Calendar.MINUTE,        (bcd[8]*10)  + bcd[9]);
        cal.set(Calendar.HOUR_OF_DAY,   (bcd[10]*10) + bcd[11]);
        cal.set(Calendar.SECOND,        (bcd[14]*10) + bcd[15]);
        
        
        Date result = null;
        
        if( bcd[0] != 0 || bcd[1] != 0 || bcd[2] != 0 || 
            bcd[3] != 0 || bcd[4] != 0 || bcd[6] != 0 ||
            bcd[7] != 0 || bcd[8] != 0 || bcd[9] != 0 ||
            bcd[10] != 0 || bcd[11] != 0 ) {
			result = cal.getTime();
		}
        
        if( debug ) {
            
            String raw =    
                "mm"    + bcd[0] + bcd[1] + 
                " yy"   + bcd[2] + bcd[3] + 
                " dd"   + bcd[6] + bcd[7] +
                " mm"   + bcd[8] + bcd[9] +
                " HH"   + bcd[10] + bcd[11] + 
                " ss"   + bcd[14] + bcd[15]; 
            
            dbg( "toDate( " + raw + " ) -> " + result );
        
        }
        
        return result; 
        
    }

    Date toPowerStreamDate(ByteArray byteArray) {
        
        Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());

        byte[] values = byteArray.getBytes();
        
        int bcd[] = new int[16];
        
        bcd[0] = (values[0]&0xf0)>>4;
        bcd[1] = (values[0]&0x0f);
        bcd[2] = (values[1]&0xf0)>>4;
        bcd[3] = (values[1]&0x0f);
        
        bcd[4] = (values[2]&0xf0)>>4;
        bcd[5] = (values[2]&0x0f);
        bcd[6] = (values[3]&0xf0)>>4;
        bcd[7] = (values[3]&0x0f);
        
        bcd[8] = (values[4]&0xf0)>>4;
        bcd[9] = (values[4]&0x0f);
        bcd[10]= (values[5]&0xf0)>>4;
        bcd[11]= (values[5]&0x0f);

        bcd[12] = (values[6]&0xf0)>>4;
        bcd[13] = (values[6]&0x0f);
        bcd[14] = (values[7]&0xf0)>>4;
        bcd[15] = (values[7]&0x0f);
        
        
        int year = (bcd[2]*10) + bcd[3];
        if( year > 60 ) {
			year += 1900;
		} else {
			year += 2000;
		}
        
        cal.set(Calendar.MONTH,         ((bcd[0]*10) + bcd[1]) -1);
        cal.set(Calendar.YEAR,          year);
        cal.set(Calendar.DAY_OF_MONTH,  (bcd[6]*10)  + bcd[7]);
        cal.set(Calendar.MINUTE,        (bcd[8]*10)  + bcd[9]);
        cal.set(Calendar.HOUR_OF_DAY,   (bcd[10]*10) + bcd[11]);
        cal.set(Calendar.SECOND,        (bcd[14]*10) + bcd[15]);
        
        
        Date result = null;
        
        if( bcd[0] != 0 || bcd[1] != 0 || bcd[2] != 0 || 
            bcd[3] != 0 || bcd[4] != 0 || bcd[6] != 0 ||
            bcd[7] != 0 || bcd[8] != 0 || bcd[9] != 0 ||
            bcd[10] != 0 || bcd[11] != 0 ) {
			result = cal.getTime();
		}
        
        if( debug ) {
            
            String raw =    
                "mm"    + bcd[0] + bcd[1] + 
                " yy"   + bcd[2] + bcd[3] + 
                " dd"   + bcd[6] + bcd[7] +
                " mm"   + bcd[8] + bcd[9] +
                " HH"   + bcd[10] + bcd[11] + 
                " ss"   + bcd[14] + bcd[15]; 
            
            dbg( "toDate( " + raw + " ) -> " + result );
        
        }
        
        return result; 
        
    }

   
    BigDecimal toBigDecimal(Type type, int[] data){
        return toBigDecimal(type, new ByteArray( data ) );
    }
    
    /*
     * A value can consist of (and ONLY of):
     *  - a Number ( = byte, word, float, ...)
     *  - a Date and a Number
     * 
     * Because of this simplification, the parsing logic stays simple/short.
     * If the type contains a date flag, the first four bytes are the date.
     * ps. mind the little endians
     *  
     */
    BigDecimal toBigDecimal(Type type, ByteArray data) {
        

//        if( (type.intValue() & Type.BCD.intValue()) > 0 ) {
////            return new BigDecimal( (byte)values[0] );
//        }
        

        /* 1 word - 2 bytes -> fits an int */ 
        if( (type.intValue() & Type.UNSIGNED_SHORT.intValue() ) > 0 ) {
            
            int i =
                (data.getBytes()[0] & 0xff ) << 8  |
                (data.getBytes()[1] & 0xff );
            
            return new BigDecimal( i );
                            
        }
        
        /* 2 words - 4 bytes -> fits a long */
        if( (type.intValue() & Type.UNSIGNED_LONG.intValue() ) > 0 ) {

            long l =
                (data.getBytes()[2] & 0xffl ) << 24 |
                (data.getBytes()[3] & 0xffl ) << 16 |
                (data.getBytes()[0] & 0xffl ) << 8  |
                (data.getBytes()[1] & 0xffl ) ;
                
        
            return new BigDecimal( l );
                            
        }
        
        if( (type.intValue() & Type.CHAR.intValue() ) > 0 ){
        	
        	char[] cha = {
        			(char) data.getBytes()[1],
        			(char) data.getBytes()[0],
        			(char) data.getBytes()[3],
        			(char) data.getBytes()[2],
        			(char) data.getBytes()[5],
        			(char) data.getBytes()[4],
        			(char) data.getBytes()[7],
        			(char) data.getBytes()[6],};
        	
        	int i = 0;
        	String str = "";
			do{
        		str = str + cha[i];
        		i++;
        	}while(cha[i] != 0);
        	
//        	String chastring = "" + cha[0] + cha[1] + cha[2] + cha[3] + cha[4] + cha[5] + cha[6] + cha[7];
			        	
        	if (debug) {
				System.out.println("chastring = " + str);
			}
        	
        	return new BigDecimal( unitInt(str) );
        	
        }
       
        String msg = "RegisterFactory.toBigDecimal() ";
        msg += "unknown type: " + type;
        throw new RuntimeException(  );
                
    }
    
    private int unitInt(String str){

    	int unitScale = 0;
    	
    	if (str.compareTo("kvar") == 0) 
    		{ unitScale = 29*10 + 3;}
    		
    	else if (str.compareTo("kvarh") == 0)
			{ unitScale = 32*10 + 3;}
    	
    	else if (str.compareTo("l/h") == 0)
			{ unitScale = 311*10 + 0;}
    	
    	else if (str.compareTo("m"+(char)(-77)+"/h") == 0)
			{ unitScale = 15*10 + 0;}
    	
    	else if (str.compareTo("kW") == 0)
			{ unitScale = 27*10 + 3;}
    	
    	else if (str.compareTo("kWh") == 0)
			{ unitScale = 30*10 + 3;}
    	
    	else if (str.compareTo("m"+(char)(-77)) == 0)
			{ unitScale = 13*10 + 0;}
    	
    	else if (str.compareTo("liters") == 0)
			{ unitScale = 310*10 + 0;}
    	
    	else if (str.compareTo("other") == 0)
			{ unitScale = 255*10 + 0;}
	
		return unitScale;
    }
    
    /* divide everything by 10 */
    BigDecimal scale( BigDecimal q ) throws IOException {
        return q.movePointLeft(1);   
    }
    
    
    boolean hasDate(Type type) {
        return (type.intValue() & Type.DATE.intValue() ) > 0;
    }
    
    /** @return short desciption of ALL the possibly available obiscodes */
    public String toString() {
        
        StringBuffer r;
        
        try {
            r = new StringBuffer();
            
            r.append( toDbgString( entry_1 ) ).append("\n");
            r.append( toDbgString( entry_2 ) ).append("\n");
            r.append( toDbgString( entry_3 ) ).append("\n");
            r.append( toDbgString( entry_4 ) ).append("\n");
            r.append( toDbgString( entry_5 ) ).append("\n");
            r.append( toDbgString( entry_6 ) ).append("\n");
            r.append( toDbgString( entry_7 ) ).append("\n");
            r.append( toDbgString( entry_8 ) ).append("\n");
            
           
            for( int i = 0; i < 12; i ++ ){
                r.append( toDbgString( entry_1_monthly[i] ) ).append("\n");
            }
            

            for( int i = 0; i < 12; i ++ ){
                r.append( toDbgString( entry_2_monthly[i]  ) ).append("\n");
            }

            for( int i = 0; i < 12; i ++ ){
                r.append( toDbgString( entry_3_monthly[i] ) ).append("\n"); 
            }
            

            for( int i = 0; i < 12; i ++ ){
                r.append( toDbgString( entry_4_monthly[i] ) ).append("\n"); 
            }
        

            for( int i = 0; i < 12; i ++ ){
                r.append( toDbgString( entry_5_monthly[i] ) ).append("\n"); 
            }
         

            for( int i = 0; i < 12; i ++ ){
                r.append( toDbgString( entry_6_monthly[i] ) ).append("\n");
            }
        

            for( int i = 0; i < 12; i ++ ){
                r.append( toDbgString( entry_7_monthly[i] ) ).append("\n");
            }
            
       
            for( int i = 0; i < 12; i ++ ){
                r.append( toDbgString( entry_8_monthly[i] ) ).append("\n");
            }
                 
            return r.toString();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null;
        
    }    
  
    /**
     * Convert a HoldingRegister to a debug string.
     */
    private String toDbgString(HoldingRegister register) throws IOException{
        String key      = register.getName();
        
        return 
            new StringBuffer()
               .append( register.getName() )
               .append( " " )
               .append( register.registerValue(key).toString() )
                   .toString();
        
    }
    
    private void dbg(Object o) {
        if( debug ) {
			System.out.println( "" + o);
		}
    }
    
    public static void main(String[] args) {
        System.out.println( new RegisterFactory(null).toString() );
    }
    
} 
