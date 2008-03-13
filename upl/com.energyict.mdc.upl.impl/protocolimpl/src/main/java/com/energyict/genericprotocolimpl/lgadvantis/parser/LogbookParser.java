package com.energyict.genericprotocolimpl.lgadvantis.parser;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.NullData;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.genericprotocolimpl.common.LogbookEvent;
import com.energyict.genericprotocolimpl.lgadvantis.Task;
import com.energyict.protocol.MeterEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

public class LogbookParser extends AbstractParser implements Parser {
	private TimeZone timeZone;

	public LogbookParser( TimeZone timeZone ) {
		this.timeZone = timeZone;
	}

	public void parse(AbstractDataType dataType, Task task) throws IOException {
		List events = parse(dataType);
		for (Iterator it = events.iterator(); it.hasNext();){
			MeterEvent event = (MeterEvent) it.next();
			task.getProfileData().addEvent(event);
		}
	}

	public List parse(AbstractDataType dataType) throws IOException {
		List result =  new ArrayList();        

		Array array = (Array)dataType.getArray();

		for( int i = 0; i < array.nrOfDataTypes(); i++ ) {

			Structure structure = (Structure)array.getDataType(i);
			if (!(structure.getDataType(0) instanceof NullData)){
				Date time = toCalendar( structure.getDataType(0) ).getTime();
				int iState = structure.getDataType(1).intValue();

				MeterEvent event = LogbookEvent.findLogbookEvent(iState).meterEvent(time);
				result.add( event ); 
			}
		}
		return result;
	}

	private Calendar toCalendar(AbstractDataType dataType) throws IOException {

		byte [] ber = dataType.getBEREncodedByteArray();
		DateTime dt = new DateTime( ber, 0, timeZone );

		return dt.getValue();

	}

}
