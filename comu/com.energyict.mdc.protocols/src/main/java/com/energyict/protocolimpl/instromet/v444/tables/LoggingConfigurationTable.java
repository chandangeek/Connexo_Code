package com.energyict.protocolimpl.instromet.v444.tables;

import com.energyict.mdc.protocol.api.device.data.ChannelInfo;

import com.energyict.cbo.Unit;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class LoggingConfigurationTable extends AbstractTable {

	private List channelInfos = new ArrayList();
	private List wrapValues = new ArrayList();


	public LoggingConfigurationTable(TableFactory tableFactory) {
		super(tableFactory);
		wrapValues = tableFactory.getInstromet444().getWrapValues();
		setChannelInfos();
	}

	protected void setChannelInfos() {
		addChannelInfo(0, "Status", Unit.getUndefined());
		addChannelInfo(1, "Temperature", Unit.get("°C"));
		addChannelInfo(2, "Pressure", Unit.get("bar"));
		addChannelInfo(3, "VE", Unit.getUndefined()); // errormeldingen
		addChannelInfo(4, "Vu", Unit.get("m3")); // gasmeterstand
		addChannelInfo(5, "V", Unit.get("m3"));
		addChannelInfo(6, "Vn", Unit.get("m3"));
	}


	protected void addChannelInfo(int id, String name, Unit unit) {
		ChannelInfo info =
			new ChannelInfo(id, "Instromet_" + (id + 1) + "_" + name, unit);
		if (wrapValues.size() >= (id + 1)) {
			BigDecimal wrapValue = (BigDecimal) wrapValues.get(id);
			if (!BigDecimal.ZERO.equals(wrapValue)) {
				info.setCumulativeWrapValue(wrapValue);
			}
			//System.out.println("wrap value: " + wrapValue);
		}
		channelInfos.add(info);
	}

	protected void parse(byte[] data) throws IOException {
		// hard coded, log selection does not matces with meter
	}

	public List getChannelInfos() {
		return channelInfos;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer("");
        for (int i = 0; i < channelInfos.size(); i++) {
        	ChannelInfo info = (ChannelInfo) channelInfos.get(i);
        	buffer.append("id = ");
        	buffer.append(info.getId());
        	buffer.append(", name = ");
        	buffer.append(info.getName());
        	buffer.append(", unit = ");
        	buffer.append(info.getUnit());
        	buffer.append(System.getProperty("line.separator"));
        }
        return buffer.toString();
	}


	public int getTableType() {
		return 10;
	}

	protected void prepareBuild() throws IOException {
		/*System.out.println("prepare build logging conf");
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.switchToLoggingConfigurationCommand().invoke();
		parseStatus(response);
    	readHeaders();*/
	}

	protected void doBuild() throws IOException {
		/*System.out.println("doBuild logging conf");
		CommandFactory commandFactory =
			getTableFactory().getCommandFactory();
		Response response =
			commandFactory.readLogSelectorCommand().invoke();
		parseStatus(response);
	    parseWrite(response);*/
	}

}
