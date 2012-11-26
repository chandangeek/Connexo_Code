package com.energyict.genericprotocolimpl.elster.ctr.validation;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 15/03/11
 * Time: 9:36
 */
public class ChannelDataExport {

    private static final String DEFAULT_PROPERTIES = "channel_data_export.properties";
    private ChannelDataExportProperties properties;
    private long now = -1;
    private final String CRLF = "\r\n";

    public ChannelDataExport(String propertiesFileName) {
        this.properties = new ChannelDataExportProperties(propertiesFileName);
    }

    public static void main(String[] args) {
        String fileName = DEFAULT_PROPERTIES;
        for (int i = 0; i < 8; i++) {
            if ((args != null) && (args.length > i) && (args[i] != null) && (args[i].trim().endsWith(".properties"))) {
                fileName = args[i].trim();
                break;
            }
        }

        ChannelDataExport export = new ChannelDataExport(fileName);
        export.doExport();

    }

    public ChannelDataExportProperties getProperties() {
        return properties;
    }

    private void doExport() {
        List<Device> rtus = getProperties().getRtus();
        for (Device rtu : rtus) {
            String outputFileName = getProperties().getOutputFolder() + rtu.getDialHomeId() + "_" + rtu.getName() + "_" + getNow() + ".txt";
            List<Channel> channels = getChannelsToExport(rtu.getChannels());
            String data = getExportedChannelData(rtu, channels);
            ProtocolTools.writeBytesToFile(outputFileName, data.getBytes(), false);
        }
    }

    private long getNow() {
        if (now == -1) {
            now = System.currentTimeMillis() / 1000;
        }
        return now;
    }

    private String getExportedChannelData(Device rtu, List<Channel> channels) {
        StringBuffer sb = new StringBuffer();
        try {
            Date installationDate = ValidationUtils.getInstallationDate(rtu, null);
            sb.append(rtu.getDialHomeId()).append(";");
            sb.append(rtu.getName()).append(";");
            sb.append(rtu.getSerialNumber()).append(";");
            sb.append(installationDate).append(";");
            sb.append(CRLF);

            Calendar currentDate = Calendar.getInstance();
            currentDate.setTime(installationDate);
            currentDate.set(Calendar.HOUR_OF_DAY, 6);
            currentDate.set(Calendar.MINUTE, 0);
            currentDate.set(Calendar.SECOND, 0);
            currentDate.set(Calendar.MILLISECOND, 0);

            ChannelDatas channelDatas = new ChannelDatas(channels, installationDate);

            while(currentDate.getTime().before(ValidationUtils.now())) {
                sb.append(currentDate.getTime()).append(";");
                sb.append(channelDatas.getIntervalInfo(currentDate.getTime()));
                sb.append(CRLF);
                currentDate.add(Calendar.HOUR_OF_DAY, 1);
            }

        } catch (BusinessException e) {
            sb.append("Unable to export channel data for rtu [" + rtu.displayString() + "]: " + e.getMessage() + CRLF);
        }
        return sb.toString();
    }

    private List<Channel> getChannelsToExport(List<Channel> channels) {
        List<Channel> channelsToExport = new ArrayList<Channel>();
        for (Channel channel : channels) {
            if (getProperties().needToExportChannel(channel)) {
                channelsToExport.add(channel);
            }
        }
        return channelsToExport;
    }

}
