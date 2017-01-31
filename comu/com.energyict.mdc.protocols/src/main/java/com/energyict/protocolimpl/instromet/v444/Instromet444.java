/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.instromet.v444;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;

import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.instromet.connection.Command;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.connection.StatusCommand;
import com.energyict.protocolimpl.instromet.core.InstrometProtocol;
import com.energyict.protocolimpl.instromet.v444.tables.RegisterFactory;
import com.energyict.protocolimpl.instromet.v444.tables.TableFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Protocol Class, supporting Instromet 444 gascorrector
 * Implements Instromet 444 protocol v1.3
 *
 * Changes:
 * JME | Fix for mantis issue #5357. Changed retrying mechanism and timeouts. Also changed the way a CRC error was caught.
 *
 * @author igh
 * @since 14/11/2007
 */
public class Instromet444 extends InstrometProtocol {

	@Override
	public String getProtocolDescription() {
		return "Instromet EVHI 444";
	}

	private Instromet444Profile instromet444Profile = null;
	private TableFactory tableFactory = null;
	private CommandFactory commandFactory=null;
	private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
	private RegisterFactory registerFactory;
	private List wrapValues = new ArrayList();
	private int iRoundtripCorrection;

	@Inject
	public Instromet444(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getInstromet444Profile().getProfileData(lastReading,includeEvents);
	}

	public RegisterFactory getRegisterFactory() throws IOException {
		if (registerFactory == null) {
			registerFactory = new RegisterFactory(this);
			registerFactory.init();
		}
		return registerFactory;
	}

	public TableFactory getTableFactory() {
		return tableFactory;
	}

	public int getRoundtripCorrection() {
		return this.iRoundtripCorrection;
	}

	protected void setWrapValues() throws IOException {
		String channelMapValue = null;
		try {
			channelMapValue = getInfoTypeChannelMap();
			if ((channelMapValue == null) || ("".equals(channelMapValue))) {
				return;
			}
			StringTokenizer tokenizer = new StringTokenizer(channelMapValue, ",");
			while (tokenizer.hasMoreTokens()) {
				wrapValues.add(new BigDecimal(tokenizer.nextToken()));
			}
		}
		catch (NumberFormatException e) {
			throw new IOException(
					"Invalid property values channelmap: should contain numbers: "
					+ channelMapValue);
		}
	}

	public Instromet444Profile getInstromet444Profile() {
		return instromet444Profile;
	}

	public void setInstromet444Profile(Instromet444Profile instromet444Profile) {
		this.instromet444Profile = instromet444Profile;
	}

	public CommandFactory getCommandFactory() {
		return commandFactory;
	}

	private void setCommandFactory(CommandFactory commandFactory) {
		this.commandFactory = commandFactory;
	}

	protected void doTheInit() throws IOException {
		this.getInstrometConnection().setNodeAddress(this.getCommId());
		setCommandFactory(new CommandFactory(this));
		setInstromet444Profile(new Instromet444Profile(this));
		tableFactory = new TableFactory(this);
		setWrapValues();
		iRoundtripCorrection=getInfoTypeRoundtripCorrection();

	}

	public List getWrapValues() {
		return wrapValues;
	}

	protected void validateSerialNumber() throws IOException {
		boolean check = true;
		if ((getInfoTypeSerialNumber() == null) ||
				("".compareTo(getInfoTypeSerialNumber())==0)) {
			return;
		}
		String sn = this.getTableFactory().getCorrectorInformationTable().getSerialNumber();
		if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
			return;
		}
		throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
	}

	public int getCommId() throws IOException {
		String nodeAddress = getInfoTypeNodeAddress();
		if ((nodeAddress == null) || ("".equals(nodeAddress))) {
			return 0;
		} else {
			try {
				return Integer.parseInt(nodeAddress);
			}
			catch (NumberFormatException e) {
				throw new IOException("invalid node address: " + nodeAddress);
			}
		}
	}


	protected void doConnect() throws IOException {
		getInstrometConnection().wakeUp();
	}

	public void parseStatus(Response response) throws IOException {
		byte[] data = response.getData();
		if (data.length < 2) {
			return;
		}
		char function = (char) data[0];
		Command command = new Command(function);
		if (command.isStatusCommand()) {
			StatusCommand statusCommand =
				new StatusCommand(tableFactory.getInstromet444());
			statusCommand.checkStatusCode(data[1]);
		}
	}

	protected void doDisConnect() throws IOException {
		/*Response response = commandFactory.logoffCommand().invoke();
		parseStatus(response);*/
	}

	protected List doGetOptionalKeys() {
		return null;
	}

	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {

	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		return getTableFactory().getCorrectorInformationTable().getFirwareVersion();
	}

    public String getProtocolVersion() {
		return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
	}

	public Date getTime() throws IOException {
		Calendar cal = Calendar.getInstance(getTimeZone());
		cal.setTime(getTableFactory().getCorrectorInformationTable().getTime());
		cal.add(Calendar.MILLISECOND, -iRoundtripCorrection);
		return cal.getTime();
	}

	public void setTime() throws IOException {
		CommandFactory cfactory = getCommandFactory();
		Response response = cfactory.switchToCorrectorInformation().invoke();
		parseStatus(response);
		response = cfactory.setTimeCommand().invoke();
		parseStatus(response);
	}

	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
		return ObisCodeMapper.getRegisterInfo(obisCode);
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		return obisCodeMapper.getRegisterValue(obisCode);
	}

	public int getNumberOfChannels() throws UnsupportedException, IOException {
		return this.tableFactory.getLoggingConfigurationTable().getChannelInfos().size();
	}

	protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
		setInstrometConnection(
				new Instromet444Connection(
						inputStream,
						outputStream,
						timeoutProperty,
						protocolRetriesProperty,
						forcedDelay,
						echoCancelling,
						halfDuplexController));
		doTheInit();
		return getInstrometConnection();
	}

}
