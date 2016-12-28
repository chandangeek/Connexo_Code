package com.energyict.protocolimpl.coronis.wavetalk.core;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
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
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.CORRECTTIME;

public abstract class AbstractWaveTalk extends AbstractProtocol implements ProtocolLink {

	public AbstractWaveTalk(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	protected abstract void doTheConnect() throws IOException;
    protected abstract void doTheInit() throws IOException;
    protected abstract void doTheDisConnect() throws IOException;

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
	protected void doDisconnect() throws IOException {
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
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.integerSpec(CORRECTTIME.getName(), false));
        return propertySpecs;
    }

    @Override
	public void setProperties(TypedProperties properties) throws PropertyValidationException {
		super.setProperties(properties);
		setInfoTypeTimeoutProperty(Integer.parseInt(properties.getTypedProperty(PROP_TIMEOUT, "20000").trim()));
		correctTime = Integer.parseInt(properties.getTypedProperty(CORRECTTIME.getName(), "0"));
	}

	@Override
	public String getFirmwareVersion() throws IOException {
        return "N/A";
	}

    public String readFirmwareVersion() throws IOException {
        return "V"+WaveflowProtocolUtils.toHexString(getRadioCommandFactory().readFirmwareVersion().getFirmwareVersion())+", Mode of transmission "+getRadioCommandFactory().readFirmwareVersion().getModeOfTransmission();
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
    public void setHalfDuplexController(HalfDuplexController halfDuplexController) {
    	// absorb
    }

    @Override
    public WaveFlowConnect getWaveFlowConnect() {
    	return waveFlowConnect;
    }

	@Override
	public Logger getLogger() {
		return super.getLogger();
	}

	@Override
	public int getInfoTypeProtocolRetriesProperty() {
		return super.getInfoTypeProtocolRetriesProperty();
	}

}