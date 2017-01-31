/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.instromet.v555;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.instromet.connection.Command;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.connection.StatusCommand;
import com.energyict.protocolimpl.instromet.core.InstrometProtocol;
import com.energyict.protocolimpl.instromet.v555.tables.RegisterFactory;
import com.energyict.protocolimpl.instromet.v555.tables.TableFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

public class Instromet555 extends InstrometProtocol {

	@Override
	public String getProtocolDescription() {
		return "Instromet EVHI 555";
	}

	private Instromet555Profile instromet555Profile = null;
	private TableFactory tableFactory = null;
    private CommandFactory commandFactory=null;
    private ObisCodeMapper obisCodeMapper = new ObisCodeMapper(this);
    private RegisterFactory registerFactory;
    private List wrapValues = new ArrayList();
    private int iRoundtripCorrection;

	@Inject
	public Instromet555(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getInstromet555Profile().getProfileData(lastReading,includeEvents);
    }

	public int getRoundtripCorrection() {
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

	protected void setWrapValues() throws IOException {
		String channelMapValue = null;
		try {
			channelMapValue = getInfoTypeChannelMap();
			if ((channelMapValue == null) || ("".equals(channelMapValue)))
				return;
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

	public Instromet555Profile getInstromet555Profile() {
        return instromet555Profile;
    }

    public void setInstromet555Profile(Instromet555Profile instromet555Profile) {
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

    public List getWrapValues() {
    	return wrapValues;
    }

    protected void validateSerialNumber() throws IOException {
        boolean check = true;
        if ((getInfoTypeSerialNumber() == null) ||
            ("".compareTo(getInfoTypeSerialNumber())==0)) return;
        String sn = this.getTableFactory().getCorrectorInformationTable().getSerialNumber();
        if (sn.compareTo(getInfoTypeSerialNumber()) == 0) return;
        throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
    }

    public int getCommId() throws IOException {
    	String nodeAddress = getInfoTypeNodeAddress();
    	if ((nodeAddress == null) || ("".equals(nodeAddress)))
    		return 0;
    	else {
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
    	if (data.length < 2)
    		return;
    	char function = (char) data[0];
    	Command command = new Command(function);
    	if (command.isStatusCommand()) {
    		StatusCommand statusCommand =
    			new StatusCommand(tableFactory.getInstromet555());
    		statusCommand.checkStatusCode((int) data[1]);
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

}
