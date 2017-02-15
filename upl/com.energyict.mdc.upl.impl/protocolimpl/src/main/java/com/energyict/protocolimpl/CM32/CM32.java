package com.energyict.protocolimpl.CM32;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class CM32 extends AbstractProtocol {

	private CM32Connection cm32Connection = null;
	private CM32Profile cm32Profile = null;
    private CommandFactory commandFactory=null;
    private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
    private RegisterFactory registerFactory;

	public CM32(PropertySpecService propertySpecService, NlsService nlsService) {
		super(propertySpecService, nlsService);
	}

	@Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getCM32Profile().getProfileData(lastReading,includeEvents);
    }

	@Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

	@Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

	@Override
    public int getNumberOfChannels() throws IOException {
        return 16;
    }

	@Override
	protected void doConnect() throws IOException {
		getLogger().info("doConnect");
		CommandFactory commandFactory = getCommandFactory();
		Response response = commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		Date time = timeTable.getTime();
		getLogger().info("time in doConnect: " + time);
	}

	@Override
	protected void doDisconnect() throws IOException {
	}

	public RegisterFactory getRegisterFactory() throws IOException {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(this);
            registerFactory.init();
        }
        return registerFactory;
    }

	@Override
	protected ProtocolConnection doInit(InputStream inputStream,
			OutputStream outputStream, int timeoutProperty,
			int protocolRetriesProperty, int forcedDelay, int echoCancelling,
			int protocolCompatible, Encryptor encryptor,
			HalfDuplexController halfDuplexController) throws IOException {
		setCM32Connection(
    			new CM32Connection(
    					inputStream,
    					outputStream,
    					timeoutProperty,
    					protocolRetriesProperty,
    					forcedDelay,
    					echoCancelling,
    					halfDuplexController));
		getCM32Connection().setCM32(this);
        setCommandFactory(new CommandFactory(this));
    	setCM32Profile(new CM32Profile(this));
    	return this.getCM32Connection();
	}

	private CM32Profile getCM32Profile() {
        return cm32Profile;
    }

    private void setCM32Profile(CM32Profile cm32Profile) {
        this.cm32Profile = cm32Profile;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

	@Override
	public String getFirmwareVersion() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getProtocolVersion() {
		return "$Date: 2015-11-26 15:25:58 +0200 (Thu, 26 Nov 2015)$";
	}

	@Override
	public Date getTime() throws IOException {
		getLogger().info("getTime");
		CommandFactory commandFactory = getCommandFactory();
		Response response = commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		Date time = timeTable.getTime();
		getLogger().info("time: " + time);
		return time;
	}

	@Override
	public void setTime() throws IOException {
	}

	CM32Connection getCM32Connection() {
        return cm32Connection;
    }

    private void setCM32Connection(CM32Connection cm32Connection) {
        this.cm32Connection = cm32Connection;
    }

}