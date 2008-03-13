package com.energyict.genericprotocolimpl.lgadvantis.parser;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.genericprotocolimpl.lgadvantis.Task;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class BillingParser extends AbstractParser implements Parser {
 
    private TimeZone timeZone;
    
    public BillingParser( TimeZone timeZone ) {
        this.timeZone = timeZone;
    }

	public void parse(AbstractDataType dataType, Task task) throws IOException {
		List registers = parse(dataType);
		
		for (Iterator it = registers.iterator(); it.hasNext();){
			RegisterValue register = (RegisterValue) it.next();
			task.addRegisterValue(register);
		}
	}
	
    public List parse(AbstractDataType dataType) throws IOException {
        
    	List result = new ArrayList();
    	
        Calendar currentTime = null;
        Array array = (Array)dataType.getArray();
        
        for( int i = 0; i < array.nrOfDataTypes(); i++ ) {
        
            Structure structure = (Structure)array.getDataType(i);
            
            if( ! (structure.getDataType(0) instanceof NullData ) ) {
                currentTime = toCalendar( structure.getDataType(0) );
            } else {   
                /* normally not necesary, but just in case */
                currentTime.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            
            Date time = currentTime.getTime();
            
            Calendar readCalendar = Calendar.getInstance();
            readCalendar.set( Calendar.MILLISECOND, 0);
            
            readCalendar.add(Calendar.SECOND, i);
            
            Date read = readCalendar.getTime();
            
            /** Landis has a status, Actaris doesn't :-S */
            if( structure.getDataType(1) instanceof Unsigned8 ) {
                parseLandis(result, structure, time, read);
            } else {
                parseActaris(result, structure, time, read);
            }
            
        }
        return result;
    }
        
    private void parseActaris(List result, Structure structure, Date time, Date read) {
    
        if( structure.nrOfDataTypes() > 1 ) {
            BigDecimal tou1  = structure.getDataType(1).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.1.VZ", tou1, time, read ) );
        }
        if( structure.nrOfDataTypes() > 2 ) {
            BigDecimal tou2  = structure.getDataType(2).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.2.VZ", tou2, time, read ) );
        }
        if( structure.nrOfDataTypes() > 3 ) {
            BigDecimal tou3  = structure.getDataType(3).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.3.VZ", tou3, time, read ) );
        }
        if( structure.nrOfDataTypes() > 4 ) {
            BigDecimal tou4  = structure.getDataType(4).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.4.VZ", tou4, time, read ) );
        }
        if( structure.nrOfDataTypes() > 5 ) {
            BigDecimal tou5  = structure.getDataType(5).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.5.VZ", tou5, time, read ) );
        }
        if( structure.nrOfDataTypes() > 6 ) {
            BigDecimal tou6  = structure.getDataType(6).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.6.VZ", tou6, time, read ) );
        }
        
    }
    
    private void parseLandis(List result, Structure structure, Date time, Date read) {
        
        if( structure.nrOfDataTypes() > 2 ) {
            BigDecimal tou1  = structure.getDataType(2).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.1.VZ", tou1, time, read ) );
        }
        if( structure.nrOfDataTypes() > 3 ) {
            BigDecimal tou2  = structure.getDataType(3).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.2.VZ", tou2, time, read ) );
        }
        if( structure.nrOfDataTypes() > 4 ) {
            BigDecimal tou3  = structure.getDataType(4).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.3.VZ", tou3, time, read ) );
        }
        if( structure.nrOfDataTypes() > 5 ) {
            BigDecimal tou4  = structure.getDataType(5).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.4.VZ", tou4, time, read ) );
        }
        if( structure.nrOfDataTypes() > 6 ) {
            BigDecimal tou5  = structure.getDataType(6).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.5.VZ", tou5, time, read ) );
        }
        if( structure.nrOfDataTypes() > 7 ) {
            BigDecimal tou6  = structure.getDataType(7).toBigDecimal();
            result.add( toRegisterValue( "1.0.1.8.6.VZ", tou6, time, read ) );
        }
        
    }
    
    private RegisterValue toRegisterValue(
            String obis, BigDecimal value, Date date, Date read ) {
        
        Unit unit = Unit.getUndefined();
        Quantity q = new Quantity( value, unit);
        ObisCode obisCode = ObisCode.fromString(obis);
        
        return new RegisterValue( obisCode, q, null, null, date, read );
    
    }
    
    private Calendar toCalendar(AbstractDataType dataType) throws IOException {
        
        byte [] ber = dataType.getBEREncodedByteArray();
        DateTime dt = new DateTime( ber, 0, timeZone );
        
        return dt.getValue();
        
    }
    
}
