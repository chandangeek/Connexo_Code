package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.coronis.core.*;

import java.io.*;
import java.util.*;

abstract public class AbstractWaveTalk extends AbstractProtocol implements ProtocolLink {

	abstract protected void doTheConnect() throws IOException;
    abstract protected void doTheInit() throws IOException;
    abstract protected void doTheDisConnect() throws IOException;
    abstract protected void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;
    
	/**
	 * reference to the lower connect latyers of the wavenis stack
	 */
	private WaveFlowConnect waveFlowConnect;

	/**
	 * reference to the radio commands factory
	 */
	private RadioCommandFactory radioCommandFactory;

	
	abstract public AbstractCommonObisCodeMapper getCommonObisCodeMapper();
	abstract public ParameterFactory getParameterFactory();
	
	/**
	 * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
	 */
	private int correctTime;

	/**
	 * The obiscode for the load profile.
	 */
	ObisCode loadProfileObisCode;
	

	final public RadioCommandFactory getRadioCommandFactory() {
		return radioCommandFactory;
	}


	@Override
	protected void doConnect() throws IOException {
		if (getExtendedLogging() >= 1) {
			getCommonObisCodeMapper().getRegisterExtendedLogging();
		}
		doTheConnect();
	}
	
	
	@Override
	protected void doDisConnect() throws IOException {
		doTheDisConnect();
	}

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		
		radioCommandFactory = new RadioCommandFactory(this);
		waveFlowConnect = new WaveFlowConnect(inputStream,outputStream,timeoutProperty,getLogger(),forcedDelay,getInfoTypeProtocolRetriesProperty());
		
		doTheInit();
		
		return waveFlowConnect;
		
	}

	@Override
	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		setInfoTypeTimeoutProperty(Integer.parseInt(properties.getProperty("Timeout","20000").trim()));
		correctTime = Integer.parseInt(properties.getProperty(MeterProtocol.CORRECTTIME,"0"));
		doTheValidateProperties(properties);
	}

	@Override
	public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "N/A";
	}

    public String readFirmwareVersion() throws IOException {
        return "V"+WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion())+", Mode of transmission "+getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission();
    }

	@Override
	public String getProtocolVersion() {
        return "$Date: 2011-12-15 15:17:05 +0100 (do, 15 dec 2011) $";
	}

	@Override
	public Date getTime() throws IOException {
        return new Date();  //Clock doesn't seem to be supported by the WaveTalk module.
	}

	@Override
	public void setTime() throws IOException {
        //Not supported
	}

	@Override
    protected List doGetOptionalKeys() {
        return new ArrayList();
    }
	
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
    	// absorb
    }

    public WaveFlowConnect getWaveFlowConnect() {
    	return waveFlowConnect;
    }
}