/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.coronis.core.ProtocolLink;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public abstract class AbstractWaveTalk extends AbstractProtocol implements ProtocolLink {

	protected abstract void doTheConnect() throws IOException;
    protected abstract void doTheInit() throws IOException;
    protected abstract void doTheDisConnect() throws IOException;
    protected abstract void doTheValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException;

	/**
	 * reference to the lower connect latyers of the wavenis stack
	 */
	private WaveFlowConnect waveFlowConnect;

	/**
	 * reference to the radio commands factory
	 */
	private RadioCommandFactory radioCommandFactory;


	public abstract AbstractCommonObisCodeMapper getCommonObisCodeMapper();
	public abstract ParameterFactory getParameterFactory();

	/**
	 * the correcttime property. this property is set from the protocolreader in order to allow to sync the time...
	 */
	private int correctTime;

	/**
	 * The obiscode for the load profile.
	 */
	ObisCode loadProfileObisCode;

	public AbstractWaveTalk(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	public final RadioCommandFactory getRadioCommandFactory() {
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
	public String getFirmwareVersion() throws IOException {
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