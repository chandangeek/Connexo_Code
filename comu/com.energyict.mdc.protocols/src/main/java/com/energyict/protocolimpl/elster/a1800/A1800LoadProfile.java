package com.energyict.protocolimpl.elster.a1800;

import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.protocolimpl.ansi.c12.tables.EventEntry;
import com.energyict.protocolimpl.ansi.c12.tables.HistoryEntry;
import com.energyict.protocolimpl.ansi.c12.tables.HistoryLog;
import com.energyict.protocolimpl.elster.a3.AlphaA3LoadProfile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class A1800LoadProfile extends AlphaA3LoadProfile {

	protected A1800 a1800;

	public A1800LoadProfile(A1800 a1800) {
		super(a1800);
		this.a1800 = a1800;
	}

	@Override
	protected MeterEvent createMeterEvent(EventEntry eventEntry) {
        EventLogMfgCodeFactory eventFact = new EventLogMfgCodeFactory();
        int eiCode = eventFact.getEICode(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag());
        String text = eventFact.getEvent(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag())+", "+
                      eventFact.getArgument(eventEntry.getEventCode().getProcedureNr(),eventEntry.getEventCode().isStdVsMfgFlag());
        int protocolCode = eventEntry.getEventCode().getProcedureNr() | (eventEntry.getEventCode().isStdVsMfgFlag()?0x8000:0);
        return new MeterEvent(eventEntry.getEventTime(),eiCode,protocolCode,text);
    }

	@Override
	protected void buildHistoryLog(ProfileData profileData, Date lastReading, Date to) throws IOException {
	       List<MeterEvent> meterHistorys = new ArrayList<MeterEvent>();
	       int validHistoryCount=0;
	       boolean futurelogcheck=true;
	       boolean leaveLoop=false;

	       while(!leaveLoop) {

	           HistoryLog historyLog = a1800.getStandardTableFactory().getHistoryLogDataTable().getHistoryLog();
	           if (historyLog.getEntries() == null) break;
	           int nrOfValidEntries = historyLog.getNrOfValidentries();

	           if (futurelogcheck) {

	               HistoryEntry[] historyEntries = historyLog.getEntries();
	               for (int i=0;i<historyEntries.length;i++) {
	                   if ((validHistoryCount++ >=(nrOfValidEntries-1)) || (historyEntries[i].getHistoryTime().before(to))) {
	                       futurelogcheck=false;
	                       break;
	                   }
	               }
	           }

	           if (!futurelogcheck) {
	               HistoryEntry[] historyEntries = historyLog.getEntries();
	               for (int i=0;i<historyEntries.length;i++) {
	                   if ((validHistoryCount++ >=(nrOfValidEntries-1)) || (historyEntries[i].getHistoryTime().before(lastReading))) {
	                       leaveLoop=true;
	                       break;
	                   }
	                   System.out.println(historyEntries[i]);
	                   meterHistorys.add(createMeterEvent(historyEntries[i]));
	               }

	           }

	       } // while(true)
	       profileData.getMeterEvents().addAll(meterHistorys);
//	       profileData.setMeterEvents(meterHistorys);

	    } // private void buildHistoryLog(ProfileData profileData, Date lastReading, Date to) throws IOException

	private MeterEvent createMeterEvent(HistoryEntry historyEntry) {
      EventLogMfgCodeFactory eventFact = new EventLogMfgCodeFactory();
      int eiCode = eventFact.getEICode(historyEntry.getHistoryCode().getProcedureNr(),historyEntry.getHistoryCode().isStdVsMfgFlag());
      String stdVsMfgStr = historyEntry.getHistoryArgument()[1] == 8?"MFG":"STD";
      String extra =  "";
      if (historyEntry.getHistoryCode().getProcedureNr()!=16)
    	  extra = stdVsMfgStr + " " + historyEntry.getHistoryArgument()[0];
	String text = eventFact.getEvent(historyEntry.getHistoryCode().getProcedureNr(),historyEntry.getHistoryCode().isStdVsMfgFlag())+", "+
                   extra;
      int protocolCode = historyEntry.getHistoryCode().getProcedureNr() | 0x7000;
      return new MeterEvent(historyEntry.getHistoryTime(),eiCode,protocolCode,text);
  }
}
