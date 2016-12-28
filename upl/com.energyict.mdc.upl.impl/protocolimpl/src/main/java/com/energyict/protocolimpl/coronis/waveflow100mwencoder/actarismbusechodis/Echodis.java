package com.energyict.protocolimpl.coronis.waveflow100mwencoder.actarismbusechodis;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.ActarisMBusInternalData;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.generic.RegisterFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;

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
	}

	@Override
	protected void doTheDisConnect() throws IOException {
	}

	public Echodis(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doTheInit() throws IOException {
		obisCodeMapper = new ObisCodeMapper(this);
		profileDataReader = new ProfileDataReader(this);
	}

	@Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
    	return obisCodeMapper.getRegisterValue(obisCode);
    }

	@Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    RegisterValue getMbusRegisterValue(ObisCode o) throws IOException {
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
	protected ProfileData getTheProfileData(Date lastReading, int portId,boolean includeEvents) throws IOException {
		return profileDataReader.getProfileData(lastReading, portId, includeEvents);
	}

	@Override
	protected MeterProtocolType getMeterProtocolType() {
		return MeterProtocolType.ECHODIS;
	}

	@Override
    protected String getRegistersInfo(int extendedLogging) throws IOException {
		return obisCodeMapper.getRegisterExtendedLogging();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

}