package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;

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

//  public static void main(String[] args) throws IOException {
//      System.out.println(com.energyict.protocolimpl.base.ToStringBuilder.genCode(new ProgrammingEventLog(null)));
//  } 	
	public String toString() {
	      // Generated code by ToStringBuilder
	      StringBuffer strBuff = new StringBuffer();
	      strBuff.append("ProgrammingEventLog:\n");
	      strBuff.append("   count="+getCount()+"\n");
	      strBuff.append("   mostRecent="+getMostRecent()+"\n");
	      for (int i=0;i<getProgrammerEventLogEntries().length;i++) {
	          strBuff.append("       programmerEventLogEntries["+i+"]="+getProgrammerEventLogEntries()[i]+"\n");
	      }
	      return strBuff.toString();
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
