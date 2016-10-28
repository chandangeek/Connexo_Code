package com.energyict.protocolimpl.CM32;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class CM32 extends AbstractProtocol {

	private CM32Connection cm32Connection = null;
	private CM32Profile cm32Profile = null;
    private CommandFactory commandFactory=null;
    private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
    private RegisterFactory registerFactory;


    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getCM32Profile().getProfileData(lastReading,includeEvents);
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    public int getNumberOfChannels() throws IOException {
        return 16;
    }


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

	protected void doValidateProperties(Properties properties)
			throws MissingPropertyException, InvalidPropertyException {
		// TODO Auto-generated method stub

	}

	public String getFirmwareVersion() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

    public String getProtocolVersion() {
		return "$Date: 2015-11-26 15:25:58 +0200 (Thu, 26 Nov 2015)$";
	}

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
