/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.ActarisMBusInternalData;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.generic.RegisterFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Properties;

public class Echodis extends WaveFlow100mW {

	@Override
	public String getProtocolDescription() {
		return "Echodis WaveFlow";
	}

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

	@Inject
	public Echodis(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doTheConnect() throws IOException {
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
					getLogger().severe(ProtocolUtils.stack2string(e));
				}

		    	registerFactories[portId] = new RegisterFactory(null);
		    	registerFactories[portId].init(ciField72h);
    		}
    	}

    	if (registerFactories[portId] == null) {
    		throw new NoSuchRegisterException("Register with obis code ["+o+"] does not exist!");
    	}
    	else {
    		RegisterValue rv = registerFactories[portId].findRegisterValue(obisCode);
    		// truncate the quantity!
    		Quantity q = rv.getQuantity();
    		if (q != null) {
    			BigDecimal bd = q.getAmount();
    			bd = bd.round(new MathContext(18,RoundingMode.HALF_UP));
    			q = new Quantity(bd, q.getUnit());
    			rv.setQuantity(q);
    		}
   			return rv;
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

	public void startMeterDetection() throws IOException {
		getRadioCommandFactory().startMeterDetection();
	}

    /**
     * Override if you want to provide info of the meter setup and registers when the "ExtendedLogging" custom property > 0
     * @param extendedLogging int
     * @throws java.io.IOException thrown when something goes wrong
     * @return String with info
     */
    protected String getRegistersInfo(int extendedLogging) throws IOException {
		return obisCodeMapper.getRegisterExtendedLogging();
    }


    @Override
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }
}
