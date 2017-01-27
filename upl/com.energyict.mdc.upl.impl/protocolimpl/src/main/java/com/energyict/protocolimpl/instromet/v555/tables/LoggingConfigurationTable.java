package com.energyict.protocolimpl.instromet.v555.tables;

import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimpl.instromet.connection.Response;
import com.energyict.protocolimpl.instromet.v555.CommandFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class LoggingConfigurationTable extends AbstractTable {
	
	private List channelInfos = new ArrayList();
	private List wrapValues = new ArrayList();
	
	static final int PRESSURE = 8;
	static final int TEMPERATURE = 12;
	static final int Z = 15;
	static final int K = 16;
	static final int CF = 17;
	static final int QL = 18;
	static final int QN = 19;
	static final int VL = 24;
	static final int VN = 25;
	static final int VEL = 26;
	static final int VEN = 27;
	static final int STATUS_NOW = 53;
	static final int STATUS_LATCHED = 54;
	
	private List codes = new ArrayList();
	
	
	public LoggingConfigurationTable(TableFactory tableFactory) {
		super(tableFactory);
		wrapValues = tableFactory.getInstromet555().getWrapValues();
	}
	
	protected void parse(byte[] data) throws IOException {
		parse(ProtocolUtils.byte2int(data[0]), 0);
		parse(ProtocolUtils.byte2int(data[1]), 1);
		parse(ProtocolUtils.byte2int(data[2]), 2);
		parse(ProtocolUtils.byte2int(data[3]), 3);
		parse(ProtocolUtils.byte2int(data[4]), 4);
	}
	
	public List getChannelInfos() {
	    boolean containsFloatingPointValue = this.containsFloatingPoint();
	    if (containsFloatingPointValue) {
	        List infos = new ArrayList();
	        int size = channelInfos.size();
            List units = new ArrayList();
            for (int i = 0; i < size; i++)
                units.add(((ChannelInfo) channelInfos.get(i)).getUnit());
	        for (int i = (size - 1); i >= 0; i--) {
                ChannelInfo info = (ChannelInfo) channelInfos.get(i);
                int id = (size - 1) - i;
                
                info.setUnit((Unit) units.get((size - 1) - i));
                //info.setId(id);
                    /*if (wrapValues.size() >= (id + 1)) {
                        BigDecimal wrapValue = (BigDecimal) wrapValues.get(id);
                    if (!new BigDecimal(0).equals(wrapValue))
                        info.setCumulativeWrapValue(wrapValue);
                    else
                        info.setCumulativeWrapValue(null);
                    }*/
                //System.out.println("wrap value: " + wrapValue);
                
                
                System.out.println("add " + ((ChannelInfo) channelInfos.get(i)).getName()
                        + ", " + ((ChannelInfo) channelInfos.get(i)).getUnit()
                        + ", " + ((ChannelInfo) channelInfos.get(i)).getId()
                        + ", " + ((ChannelInfo) channelInfos.get(i)).getCumulativeWrapValue());
	            infos.add(channelInfos.get(i));
	        }
            return channelInfos;
	    }
	    else
	        return channelInfos;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer("");
        List infos = getChannelInfos();
        for (int i = 0; i < infos.size(); i++) {
            ChannelInfo info = (ChannelInfo) infos.get(i);
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
	
	protected void parse(int value, int id) throws IOException  {
		if (value == 0)
			return;
		ChannelInfo info = 
			new ChannelInfo(id, "Instromet_" + (id + 1) + "_" + value, getUnit(value));
		if (wrapValues.size() >= (id + 1)) {
			BigDecimal wrapValue = (BigDecimal) wrapValues.get(id);
			if (!new BigDecimal(0).equals(wrapValue))
				info.setCumulativeWrapValue(wrapValue);
			//System.out.println("wrap value: " + wrapValue);
		}
		channelInfos.add(info);
		codes.add(new Integer(value));
	}
	
	protected boolean containsFloatingPoint() {
	    int size = codes.size();
	    for (int i = 0; i < size; i++) {
	        if (isFloatingPoint(i))
	            return true;
	    }
	    return false;
	}
	
	protected boolean isFloatingPoint(int index) { // zero based index!
		int code = ((Integer) codes.get(index)).intValue();
		return (code == PRESSURE) || (code == TEMPERATURE) || (code == CF);
	}
	
	protected Unit getUnit(int value) throws IOException {
        switch (value) {
            case PRESSURE:  return Unit.get("bar");
            case TEMPERATURE:  return Unit.get("°C");
            //case Z:  break;
            //case K:  break;
            //case CF:  break;
            //case QL:  break;
            // case QN:  break;
            case VL:  return Unit.get("m3");
            case VN:  return Unit.get("m3");
            case VEL:  return Unit.get("m3");
            case VEN:  return Unit.get("m3");
            case CF:  return Unit.getUndefined();
            //case STATUS_NOW: break;
            //case STATUS_LATCHED: break;
            default: throw new IOException(
            		"Logging configuration table: Invalid logged data code (" + 
            		value + ")"  + ", not supported");
        }
	}
	
	public int getTableType() {
		return 10;
	}
	
	protected void prepareBuild() throws IOException {
		System.out.println("prepare build logging conf");
		CommandFactory commandFactory = 
			getTableFactory().getCommandFactory();
		Response response = 
			commandFactory.switchToLoggingConfigurationCommand().invoke();
		parseStatus(response);
    	readHeaders();
	}
	
	protected void doBuild() throws IOException {
		System.out.println("doBuild logging conf");
		CommandFactory commandFactory = 
			getTableFactory().getCommandFactory();
		Response response = 
			commandFactory.readLogSelectorCommand().invoke();
		parseStatus(response);
	    parseWrite(response);
	}

}
