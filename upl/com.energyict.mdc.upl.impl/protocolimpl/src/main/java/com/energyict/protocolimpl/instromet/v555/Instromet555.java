package com.energyict.protocolimpl.instromet.v555;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.instromet.connection.Command;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.connection.StatusCommand;
import com.energyict.protocolimpl.instromet.core.InstrometProtocol;
import com.energyict.protocolimpl.instromet.v555.tables.RegisterFactory;
import com.energyict.protocolimpl.instromet.v555.tables.TableFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class Instromet555 extends InstrometProtocol implements SerialNumberSupport {

	private Instromet555Profile instromet555Profile = null;
	private TableFactory tableFactory = null;
    private CommandFactory commandFactory=null;
    private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
    private RegisterFactory registerFactory;
    private List<BigDecimal> wrapValues = new ArrayList<>();
    private int iRoundtripCorrection;

	public Instromet555(PropertySpecService propertySpecService, NlsService nlsService) {
		super(propertySpecService, nlsService);
	}

	@Override
	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getInstromet555Profile().getProfileData(lastReading,includeEvents);
    }

	int getRoundtripCorrection() {
		return this.iRoundtripCorrection;
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

	private Instromet555Profile getInstromet555Profile() {
        return instromet555Profile;
    }

    private void setInstromet555Profile(Instromet555Profile instromet555Profile) {
        this.instromet555Profile = instromet555Profile;
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
    	setInstromet555Profile(new Instromet555Profile(this));
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

    public void parseStatus(Response response) throws IOException {
    	byte[] data = response.getData();
    	if (data.length < 2) {
		    return;
	    }
    	char function = (char) data[0];
    	Command command = new Command(function);
    	if (command.isStatusCommand()) {
    		StatusCommand statusCommand =
    			new StatusCommand(tableFactory.getInstromet555());
    		statusCommand.checkStatusCode((int) data[1]);
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
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries()+1);
        }
    }

	@Override
    public String getProtocolVersion() {
		return "$Date: 2015-11-26 15:26:00 +0200 (Thu, 26 Nov 2015)$";
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

}