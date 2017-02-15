package com.energyict.protocolimpl.instromet.v444;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.instromet.connection.Command;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.connection.StatusCommand;
import com.energyict.protocolimpl.instromet.core.InstrometProtocol;
import com.energyict.protocolimpl.instromet.v444.tables.RegisterFactory;
import com.energyict.protocolimpl.instromet.v444.tables.TableFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
public class Instromet444 extends InstrometProtocol implements SerialNumberSupport {

	private Instromet444Profile instromet444Profile = null;
	private TableFactory tableFactory = null;
	private CommandFactory commandFactory=null;
	private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
	private RegisterFactory registerFactory;
	private List<BigDecimal> wrapValues = new ArrayList<>();
	private int iRoundtripCorrection;

	public Instromet444(PropertySpecService propertySpecService, NlsService nlsService) {
		super(propertySpecService, nlsService);
	}

	@Override
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

	int getRoundtripCorrection() {
		return this.iRoundtripCorrection;
	}

	private void setWrapValues() throws IOException {
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

	private Instromet444Profile getInstromet444Profile() {
		return instromet444Profile;
	}

	private void setInstromet444Profile(Instromet444Profile instromet444Profile) {
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

	public List<BigDecimal> getWrapValues() {
		return wrapValues;
	}

	private int getCommId() throws IOException {
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

	@Override
	protected void doConnect() throws IOException {
		getInstrometConnection().wakeUp();
	}

	public void parseStatus(Response response) throws ProtocolException {
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

	@Override
	protected void doDisconnect() throws IOException {
	}

	@Override
	public String getFirmwareVersion() throws IOException {
		return getTableFactory().getCorrectorInformationTable().getFirwareVersion();
	}

    @Override
    public String getSerialNumber() {
        try {
            return this.getTableFactory().getCorrectorInformationTable().getSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

	@Override
    public String getProtocolVersion() {
		return "$Date: 2015-11-26 15:25:14 +0200 (Thu, 26 Nov 2015)$";
	}

	@Override
	public Date getTime() throws IOException {
		Calendar cal = Calendar.getInstance(getTimeZone());
		cal.setTime(getTableFactory().getCorrectorInformationTable().getTime());
		cal.add(Calendar.MILLISECOND, -iRoundtripCorrection);
		return cal.getTime();
	}

	@Override
	public void setTime() throws IOException {
		CommandFactory cfactory = getCommandFactory();
		Response response = cfactory.switchToCorrectorInformation().invoke();
		parseStatus(response);
		response = cfactory.setTimeCommand().invoke();
		parseStatus(response);
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
		return this.tableFactory.getLoggingConfigurationTable().getChannelInfos().size();
	}

	@Override
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