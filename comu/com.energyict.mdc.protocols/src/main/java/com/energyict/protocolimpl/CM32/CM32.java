package com.energyict.protocolimpl.CM32;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class CM32 extends AbstractProtocol {

	@Override
	public String getProtocolDescription() {
		return "Siemens Energy Services Ltd CM32";
	}

	private CM32Connection cm32Connection = null;
	private CM32Profile cm32Profile = null;
    private CommandFactory commandFactory=null;
    private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
    private RegisterFactory registerFactory;

	@Inject
	public CM32(PropertySpecService propertySpecService) {
		super(propertySpecService);
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
    protected void validateSerialNumber() throws IOException {
	}

	@Override
	protected void doConnect() throws IOException {
		getLogger().info("doConnect");

		CommandFactory commandFactory = getCommandFactory();
		Response response =
			commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		Date time = timeTable.getTime();
		getLogger().info("time in doConnect: " + time);
	}

	@Override
	protected void doDisConnect() throws IOException {
	}

	protected List<String> doGetOptionalKeys() {
		return Collections.emptyList();
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

	public CM32Profile getCM32Profile() {
        return cm32Profile;
    }

    public void setCM32Profile(CM32Profile cm32Profile) {
        this.cm32Profile = cm32Profile;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    private void setCommandFactory(CommandFactory commandFactory) {
        this.commandFactory = commandFactory;
    }

	@Override
	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getFirmwareVersion() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public String getProtocolVersion() {
		return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
	}

	@Override
	public Date getTime() throws IOException {
		getLogger().info("getTime");
		CommandFactory commandFactory = getCommandFactory();
		Response response =
			commandFactory.getReadTimeCommand().invoke();
		TimeTable timeTable = new TimeTable(this);
		timeTable.parse(response.getData());
		Date time = timeTable.getTime();
		getLogger().info("time: " + time);
		return time;
	}

	@Override
	public void setTime() throws IOException {
		// TODO Auto-generated method stub

	}

	public CM32Connection getCM32Connection() {
        return cm32Connection;
    }

    protected void setCM32Connection(CM32Connection cm32Connection) {
        this.cm32Connection = cm32Connection;
    }

}