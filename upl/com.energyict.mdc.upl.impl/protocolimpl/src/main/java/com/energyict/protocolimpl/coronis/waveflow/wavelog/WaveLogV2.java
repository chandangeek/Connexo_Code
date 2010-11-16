package com.energyict.protocolimpl.coronis.waveflow.wavelog;

import java.io.IOException;
import java.util.*;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;

public class WaveLogV2 extends WaveFlow {

	/**
	 * read and build the profiledata
	 */
	private ProfileDataReader profileDataReader;

	
	@Override
	protected void doTheInit() throws IOException {
		profileDataReader = new ProfileDataReader(this);
	}	
	
	@Override
	protected void doTheConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doTheDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doTheValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected MeterProtocolType getMeterProtocolType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ProfileData getTheProfileData(Date lastReading, int portId,boolean includeEvents) throws UnsupportedException, IOException {
		return profileDataReader.getProfileData(lastReading, portId, includeEvents);
	}
	
}
