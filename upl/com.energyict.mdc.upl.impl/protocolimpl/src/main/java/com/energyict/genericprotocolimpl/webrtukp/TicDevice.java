package com.energyict.genericprotocolimpl.webrtukp;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.genericprotocolimpl.nta.profiles.TicEventProfile;
import com.energyict.genericprotocolimpl.nta.profiles.TicProfile;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TicDevice implements GenericProtocol {
	
	private Rtu tic;
	private Logger logger;
	private AbstractNTAProtocol webRtu;
	
	public TicDevice(){
	}
	
	public TicDevice(Rtu tic) {
		this.tic = tic;
	}

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.logger = logger;
		String profileOc = this.tic.getProperties().getProperty("LoadProfileObiscode", "1.0.99.2.0.255");
		String eventOc = this.tic.getProperties().getProperty("EventProfileObisCode","0.0.99.98.50.255");
		
		if(scheduler.getCommunicationProfile().getReadDemandValues()){
			this.logger.log(Level.INFO, "Getting loadProfile from TicDevice");
			TicProfile tp = new TicProfile(this);
			ProfileData pd = tp.getProfileData(ObisCode.fromString(profileOc));
			getWebRTU().getStoreObject().add(pd, tic);
		}
		
		if(scheduler.getCommunicationProfile().getReadMeterEvents()){
			this.logger.log(Level.INFO, "Getting events from TicDevice");
			TicEventProfile tep = new TicEventProfile(this);
			ProfileData epd = tep.getEvents(ObisCode.fromString(eventOc));
			getWebRTU().getStoreObject().add(epd, tic);
		}
		
	}

	public long getTimeDifference() {
		return 0;
	}

	public void addProperties(Properties properties) {
		
	}

	public String getVersion() {
		return "$Date$";
	}

	public List getOptionalKeys() {
		List result = new ArrayList(2);
		result.add("LoadProfileObisCode");
		result.add("EventProfileObisCode");
		return result;
	}

	public List getRequiredKeys() {
		return new ArrayList(0);
	}

	public void setWebRTU(AbstractNTAProtocol webRTUKP) {
		this.webRtu = webRTUKP;
	}
	
	public AbstractNTAProtocol getWebRTU(){
		return this.webRtu;
	}
	
	public Rtu getMeter(){
		return this.tic;
	}

}
