package com.energyict.genericprotocolimpl.webrtuz3.profiles;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import com.energyict.dlms.DataContainer;
import com.energyict.genericprotocolimpl.webrtuz3.TicDevice;
import com.energyict.genericprotocolimpl.webrtuz3.eventhandling.TicLog;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;

public class TicEventProfile {

	private TicDevice tic;
	
	public TicEventProfile(TicDevice ticDevice) {
		this.tic = ticDevice;
	}

	public ProfileData getEvents(ObisCode eventObisCode) throws IOException {
		ProfileData profileData = new ProfileData();
		
		Date lastLogReading = this.tic.getMeter().getLastLogbook();
		if(lastLogReading == null){
			lastLogReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(this.tic.getMeter());
		}
		Calendar fromCal = ProtocolUtils.getCleanCalendar(this.tic.getWebRTU().getTimeZone());
		fromCal.setTime(lastLogReading);
		this.tic.getWebRTU().getLogger().log(Level.INFO, "Reading EVENTS from TicDevice");
		DataContainer ticContainter = this.tic.getWebRTU().getCosemObjectFactory().getProfileGeneric(eventObisCode).getBuffer(fromCal, this.tic.getWebRTU().getToCalendar());
		
		TicLog ticLog = new TicLog(ticContainter);
		profileData.getMeterEvents().addAll(ticLog.getMeterEvents());
		
		return profileData;
	}

}
