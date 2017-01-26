package com.energyict.protocolimpl.modbus.enerdis.cdt;

import com.energyict.cbo.ApplicationException;
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

class RegisterFactory  extends AbstractRegisterFactory {

    private final static boolean debug = false;
    
    static final Unit percent   = Unit.get(BaseUnit.PERCENT, 1);
    static final Unit fpUnit    = Unit.get(BaseUnit.UNITLESS);
    static final Unit kvarh     = Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 3);
    static final Unit kVAh      = Unit.get(BaseUnit.VOLTAMPEREHOUR, 3);
    static final Unit VA        = Unit.get(BaseUnit.VOLTAMPERE);
    static final Unit VAr       = Unit.get(BaseUnit.VOLTAMPEREREACTIVE);
    static final Unit W         = Unit.get(BaseUnit.WATT);
    static final Unit A         = Unit.get(BaseUnit.AMPERE);
    static final Unit V         = Unit.get(BaseUnit.VOLT);
    static final Unit kWh       = Unit.get(BaseUnit.WATTHOUR, 3);
    
    private RecDigitCdt recDigit;
    
    public RegisterFactory(Modbus modBus) {
        super(modBus);
        
        recDigit = (RecDigitCdt) modBus;
    }
    
    /** create a HoldingRegister (=factory method) */
    HoldingRegister add( 
        String obis, int address, String description, Unit unit, Type type ){
        
        ObisCode oc   = ObisCode.fromString(obis);
        int wordSize  = type.wordSize();
        
        HoldingRegister hr = 
            new HoldingRegister( 
                address, wordSize, oc, unit, description);
        hr.setRegisterFactory(this);
        hr.setParser(type.toString());
        
        getRegisters().add( hr );

        return hr;
        
    }
    
    protected void initParsers() {
        
        
        getParserFactory().addParser(Type.BYTE.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) 
                throws IOException {
                
                dbg( "Parser.val( " + Type.BYTE + " )" );
                BigDecimal bd = toBigDecimal(Type.BYTE, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );

                return new RegisterValue( register.getObisCode(), q );
                
            }
        });


        getParserFactory().addParser(Type.WORD.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) 
                throws IOException {
                
                dbg( "Parser.val( " + Type.WORD + " )" );
                BigDecimal bd = toBigDecimal(Type.WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                return new RegisterValue( register.getObisCode(), q );
            }
        });

        getParserFactory().addParser(Type.LONG_WORD.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) 
                throws IOException {
                
                dbg( "Parser.val( " + Type.LONG_WORD + " )" );
                
                BigDecimal bd = toBigDecimal(Type.LONG_WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                return new RegisterValue( register.getObisCode(), q );
            }
        });

        getParserFactory().addParser(Type.LONG_DOUBLE_WORD.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) 
                throws IOException {
                
                dbg( "Parser.val( " + Type.LONG_DOUBLE_WORD + " )" );
                
                BigDecimal bd = toBigDecimal(Type.LONG_DOUBLE_WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                return new RegisterValue( register.getObisCode(), q );
            }
        });

        getParserFactory().addParser(Type.REAL_NUMBER.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) {
            
                dbg( "Parser.val( " + Type.REAL_NUMBER + " )" );
                
                try {
                    BigDecimal bd = toBigDecimal(Type.REAL_NUMBER, values);
                    Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                    return new RegisterValue( register.getObisCode(), q );
                } catch( IOException ioe ){
                    throw new ApplicationException(ioe);
                }
            }
        });

        getParserFactory().addParser(Type.DATE.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) {
                
                dbg( "Parser.val( " + Type.DATE  + " )" );
                Date date = toDate(values); 
                return new RegisterValue( register.getObisCode(), date );
            }
        });

        getParserFactory().addParser(Type.DATE_AND_WORD.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) 
                throws IOException {
                
                dbg( "Parser.val( " + Type.DATE_AND_WORD  + " )" );

                BigDecimal bd = toBigDecimal(Type.DATE_AND_WORD, values);
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                Date date = toDate(values); 
                return new RegisterValue( register.getObisCode(), q, date );
                }
        });

        getParserFactory().addParser(Type.DATE_AND_LONG_WORD.toString(), 
        new Parser() {
            public Object val(int[] values, AbstractRegister register) 
                throws IOException {
                
                dbg( "Parser.val( " + Type.DATE_AND_LONG_WORD  + " )" );
                
                BigDecimal bd = toBigDecimal(Type.DATE_AND_LONG_WORD, values);       
                Quantity q = scale( new Quantity( bd, register.getUnit() ) );
                Date date = toDate(values); 
                return new RegisterValue( register.getObisCode(), q, date );
            }
        });
    } 
    
    
    
    Date toDate(int[] values) {
        
        Calendar cal = ProtocolUtils.getCleanCalendar(getModBus().getTimeZone());
        int bcd[] = new int[12];
        
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
        
        int year = (bcd[2]*10) + bcd[3];
        if( year > 60 ) 
            year += 1900;
        else
            year += 2000;
        
        cal.set(Calendar.MONTH,         ((bcd[0]*10) + bcd[1]) -1);
        cal.set(Calendar.YEAR,          year);
        cal.set(Calendar.HOUR_OF_DAY,   (bcd[4]*10) + bcd[5]);
        cal.set(Calendar.DAY_OF_MONTH,  (bcd[6]*10) + bcd[7]);
        cal.set(Calendar.SECOND,        (bcd[8]*10) + bcd[9]);
        cal.set(Calendar.MINUTE,        (bcd[10]*10)+ bcd[11]);
        
        Date result = null;
        
        if( bcd[0] != 0 || bcd[1] != 0 || bcd[2] != 0 || 
            bcd[3] != 0 || bcd[4] != 0 || bcd[6] != 0 ||
            bcd[7] != 0 || bcd[8] != 0 || bcd[9] != 0 ||
            bcd[10] != 0 || bcd[11] != 0 )
            
            result = cal.getTime();
        
        if( debug ) {
            
            String raw =    
                
                "mm"    + bcd[0] + bcd[1] + 
                " yy"   + bcd[2] + bcd[3] + 
                " HH"   + bcd[4] + bcd[5] + 
                " dd"   + bcd[6] + bcd[7] + 
                " ss"   + bcd[8] + bcd[9] + 
                " mm"   + bcd[10] + bcd[11];
            
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
        if( year > 60 ) 
            year += 1900;
        else
            year += 2000;
        
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
            bcd[10] != 0 || bcd[11] != 0 )
            
            result = cal.getTime();
        
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
   
    BigDecimal toBigDecimal(Type type, ByteArray byteArray) {

        byte[] data = byteArray.getBytes();
        
        if( (type.intValue() & Type.WORD.intValue()) > 0 ) {
            return new BigDecimal( (data[0] << 8) | data[1]  );
        }

        if( (type.intValue() & Type.LONG_WORD.intValue() ) > 0 ) { 

            long l =
                (data[2] & 0xffl ) << 24 |
                (data[3] & 0xffl ) << 16 |
                (data[0] & 0xffl ) << 8  |
                (data[1] & 0xffl ) ;
                
        
            return new BigDecimal( l );
                            
        }
        
        String msg = "RegisterFactory.toBigDecimal() ";
        msg += "unknown type: " + type;
        throw new RuntimeException(  );
              
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
    BigDecimal toBigDecimal(Type type, int values[]) {
        
        int [] data = null;
        
        if( hasDate(type) ) {
       
            int newLength = values.length - Type.DATE.wordSize();
            data = new int[newLength];
            System.arraycopy(values, Type.DATE.wordSize(), data, 0, newLength);
            
        } else {
            
            data = values;
            
        }
            
        if( (type.intValue() & Type.BYTE.intValue()) > 0 ) {
            /* cast NOT bitwise and, since it is a SIGNED byte */
            return new BigDecimal( (byte)values[0] );
        }
        
        if( (type.intValue() & Type.WORD.intValue()) > 0 )
            return new BigDecimal(  data[0]  );

        if( (type.intValue() & Type.LONG_WORD.intValue() ) > 0 ) {

            int i =  
                ( ( data[1] & 0x0000ffff ) << 16 ) | 
                  ( data[0] & 0x0000ffff );    
        
            return new BigDecimal( i );
                            
        }
        
        if( (type.intValue() & Type.LONG_DOUBLE_WORD.intValue()) > 0 ) {        

            long i =  
                (data[3] & 0x000000000000ffffl) << 48 |
                (data[2] & 0x000000000000ffffl) << 32 |
                (data[1] & 0x000000000000ffffl) << 16 |
                (data[0] & 0x000000000000ffffl);
        
            return new BigDecimal(i);
            
        }
        
        if( ( type.intValue() & Type.REAL_NUMBER.intValue()) > 0 ) {
            
            int i =  
                ( ( data[1] & 0x0000ffff ) << 16 ) | 
                  ( data[0] & 0x0000ffff );      

            return new BigDecimal( Float.intBitsToFloat(i) );
            
        }
                
        String msg = "RegisterFactory.toBigDecimal() ";
        msg += "unknown type: " + type;
        throw new RuntimeException(  );
                
    }
    
    Quantity scale( Quantity q ) throws IOException {
        
        Unit unit = q.getUnit();
        BigDecimal amount = q.getAmount();
        int round = BigDecimal.ROUND_HALF_UP;
        int scale = amount.scale() + 3;
        
        if(  V.equals( unit ) ) {
            
            BigDecimal bd = amount.divide(recDigit.getKU(), scale, round);
            bd = bd.multiply(recDigit.getPtRatio());
            
            return new Quantity( bd, unit );
            
        }
        
        if( A.equals( unit ) ) {
            
            BigDecimal bd = amount.divide(recDigit.getKI(), scale, round);
            bd = bd.multiply(recDigit.getCtRatio());
            
            return new Quantity( bd, unit );
            
        }

        if( W.equals(unit) || VAr.equals(unit) ) {
            
            BigDecimal bd = amount.divide(recDigit.getKP(), scale, round);
            bd = bd.multiply(recDigit.getCtRatio());
            bd = bd.multiply(recDigit.getPtRatio());
            
            return new Quantity( bd, unit );
            
        }
        
        if( kWh.equals(unit) || kvarh.equals(unit) ) {
            
            BigDecimal bd = amount.divide(recDigit.getKP(), scale, round);
            bd = bd.multiply(recDigit.getCtRatio());
            bd = bd.multiply(recDigit.getPtRatio());
            bd = bd.divide(new BigDecimal( 3600000 ), scale, round); 
            
            return new Quantity( bd, unit );
            
        }

        return q;
        
    }
    
    
    boolean hasDate(Type type) {
        return (type.intValue() & Type.DATE.intValue() ) > 0;
    }

    protected void init() {
        throw new RuntimeException( "" );
        
    }
    
    private void dbg(Object o) {
        if( debug ) System.out.println( "" + o);
    }
    
    public static void main(String[] args) {
        System.out.println( new RegisterFactory(null).toString() );
    }
    
}
