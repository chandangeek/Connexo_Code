package com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis;

import java.io.IOException;
import java.util.*;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.*;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW.MeterProtocolType;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.generic.RegisterFactory;

public class Echodis extends WaveFlow100mW {

	/** 
	 * Actaris specific obis code mapper
	 */
	private ObisCodeMapper obisCodeMapper;
	
	/**
	 * read and build the profiledata
	 */
	private ProfileDataReader profileDataReader;	
	
	/**
	 * MBus register factory. This contains the parsed data read from the waveflow represeiting MBus CI72 data
	 */
	private RegisterFactory[] registerFactories=new RegisterFactory[2];
	
	@Override
	protected void doTheConnect() throws IOException {
		if (getExtendedLogging() >= 1) {
			getLogger().info(obisCodeMapper.getRegisterExtendedLogging());
		}
	}


	@Override
	protected void doTheDisConnect() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doTheInit() throws IOException {
		obisCodeMapper = new ObisCodeMapper(this);
		profileDataReader = new ProfileDataReader(this);
	}
	
	@Override
	protected void doTheValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		
	}
	
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	return obisCodeMapper.getRegisterValue(obisCode);
    }	 

    /**
     * Override this method to provide meter specific info for an obiscode mapped register. This method is called outside the communication session. So the info provided is static info in the protocol.
     * @param obisCode obiscode of the register to lookup
     * @throws java.io.IOException thrown when somethiong goes wrong
     * @return RegisterInfo object
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterInfo(obisCode);
    }
    
    public RegisterValue getMbusRegisterValue(ObisCode o) throws IOException {
    	
    	int portId = o.getB()==0?0:1;
    	ObisCode obisCode = new ObisCode(o.getA(), 0, o.getC(), o.getD(), o.getE(), o.getF());
    	
    	if (registerFactories[portId] == null) {
    		ActarisMBusInternalData internalData = (ActarisMBusInternalData)readInternalDatas()[portId];
    		if (internalData != null) {
		        CIField72h ciField72h = new CIField72h(getTimeZone());
		        try {
					ciField72h.parse(WaveflowProtocolUtils.getSubArray(internalData.getEncoderInternalData(),3));
				} catch (IOException e) {
					getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
				}
		    	
		    	registerFactories[portId] = new RegisterFactory(null);
		    	registerFactories[portId].init(ciField72h);
    		}
    	}
    	
    	if (registerFactories[portId] == null) {
    		throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");
    	}
    	else {
    		return registerFactories[portId].findRegisterValue(obisCode);
    	}
    }

	@Override
	protected ProfileData getTheProfileData(Date lastReading, int portId,boolean includeEvents) throws UnsupportedException, IOException {
		return profileDataReader.getProfileData(lastReading, portId, includeEvents);
	}    

	@Override
	protected MeterProtocolType getMeterProtocolType() {
		return MeterProtocolType.ECHODIS;
	}
	
}
