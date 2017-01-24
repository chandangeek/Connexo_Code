package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.TimeZone;

public class ProgrammingEventLog extends AbstractEventLog {

	final int DEBUG=0;

	int mostRecent;
	int count;
	final int MAX_ENTRIES=10;
	ProgrammerEventLogEntry[] programmerEventLogEntries;


	public ProgrammingEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
	}

	public String toString() {
	      // Generated code by ToStringBuilder
	      StringBuilder builder = new StringBuilder();
	      builder.append("ProgrammingEventLog:\n");
	      builder.append("   count=").append(getCount()).append("\n");
	      builder.append("   mostRecent=").append(getMostRecent()).append("\n");
	      for (int i=0;i<getProgrammerEventLogEntries().length;i++) {
	          builder.append("       programmerEventLogEntries[").append(i).append("]=").append(getProgrammerEventLogEntries()[i]).append("\n");
	      }
	      return builder.toString();
	  }

	public void parse(byte[] data) throws IOException {
		int offset=0;
		mostRecent = ProtocolUtils.getIntLE(data, offset++, 1);
		count = ProtocolUtils.getIntLE(data, offset, 2); offset+=2;
        programmerEventLogEntries = new ProgrammerEventLogEntry[MAX_ENTRIES];
        for(int i=0;i<MAX_ENTRIES;i++) {
        	programmerEventLogEntries[i] = new ProgrammerEventLogEntry(data,offset, getTimeZone());
        	offset+=ProgrammerEventLogEntry.size();
        	//System.out.println(programmerEventLogEntries[i]);
        	addMeterEvent(new MeterEvent(programmerEventLogEntries[i].getTimeStampIndex(), MeterEvent.CONFIGURATIONCHANGE,programmerEventLogEntries[i].getProgrammerIndex()+" ("+count+")"));
        }
	}

	public ProgrammerEventLogEntry[] getProgrammerEventLogEntries() {
		return programmerEventLogEntries;
	}

	public int getMostRecent() {
		return mostRecent;
	}

	public int getCount() {
		return count;
	}

}
