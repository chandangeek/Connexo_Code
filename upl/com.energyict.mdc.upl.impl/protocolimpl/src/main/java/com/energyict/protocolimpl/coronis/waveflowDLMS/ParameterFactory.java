package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.IOException;
import java.util.*;

import com.energyict.protocolimpl.coronis.core.ProtocolLink;

public class ParameterFactory {

	
	private final ProtocolLink protocolLink;

	ParameterFactory(ProtocolLink protocolLink) {
		this.protocolLink = protocolLink;
	}
	

	final public Date readTimeDateRTC() throws IOException {
		TimeDateRTC o = new TimeDateRTC(protocolLink);
		o.read();
		return o.getCalendar().getTime();
	}
	
	final public void writeTimeDateRTC(final Date date) throws IOException {
		TimeDateRTC o = new TimeDateRTC(protocolLink);
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT")); //protocolLink.getTimeZone());
		calendar.setTime(date);
		o.setCalendar(calendar);
		o.write();
	}
	
}
