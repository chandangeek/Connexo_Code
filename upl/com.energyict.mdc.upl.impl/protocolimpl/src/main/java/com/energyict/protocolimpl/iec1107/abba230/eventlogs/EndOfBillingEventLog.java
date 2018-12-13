package com.energyict.protocolimpl.iec1107.abba230.eventlogs;

import com.energyict.protocol.MeterEvent;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.TimeZone;

public class EndOfBillingEventLog extends AbstractEventLog {

	int mostRecent;
	int count;
	TimeStampInfoPair[] timeStampInfoPairs = new TimeStampInfoPair[10];

    private HashMap<Integer, String> billingEventSource = new HashMap<Integer, String>(6);

	public EndOfBillingEventLog(TimeZone timeZone) throws IOException {
		super(timeZone);
        billingEventSource.put(new Integer(0x01), "Timed Billing Event");
        billingEventSource.put(new Integer(0x02), "Season Start Billing Event");
        billingEventSource.put(new Integer(0x04), "Tariff Changeover Billing Event");
        billingEventSource.put(new Integer(0x08), "Remote Port Commanded Billing");
        billingEventSource.put(new Integer(0x10), "FLAG Port Commanded Billing");
        billingEventSource.put(new Integer(0x20), "Battery Fail Billing");
	}
	
	public void parse(byte[] data) throws IOException {
		int offset=0;
		mostRecent = ProtocolUtils.getIntLE(data, offset++, 1);
        count = ProtocolUtils.getInt(data, offset, 2);
        offset += 2;
        for (int i = 0; i < 10; i++) {
            timeStampInfoPairs[i] = new TimeStampInfoPair(data, offset, timeZone);
            offset += TimeStampInfoPair.size();
            if (timeStampInfoPairs[i].getDate() != null) {
                String billingSource = billingEventSource.get(new Integer(timeStampInfoPairs[i].getInfoIndex()));
                addMeterEvent(new MeterEvent(timeStampInfoPairs[i].getDate(), MeterEvent.BILLING_ACTION, billingSource == null ? "Invalid event source: " + timeStampInfoPairs[i].getInfoIndex() : billingSource + " (" + count + ")"));
            }
        }
    }

    public int getCount() {
        return count;
    }
}
