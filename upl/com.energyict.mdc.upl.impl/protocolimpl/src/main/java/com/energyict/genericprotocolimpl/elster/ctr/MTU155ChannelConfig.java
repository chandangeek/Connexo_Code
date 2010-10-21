package com.energyict.genericprotocolimpl.elster.ctr;

/**
 * Copyrights EnergyICT
 * Date: 19-okt-2010
 * Time: 15:32:11
 */
public class MTU155ChannelConfig {

    private final int numberOfChannels;
    private final String[] channelIds;
    private static final String IGNORE_CHANNEL = "-";

    public MTU155ChannelConfig(String channelConfig) {
        this.channelIds = channelConfig.split(":");
        this.numberOfChannels = channelIds.length;
    }

    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    public String[] getChannelIds() {
        return channelIds;
    }

    public String getChannelObjectId(int channelIndex) {
        if (getNumberOfChannels() > channelIndex) {
            String id = channelIds[channelIndex];
            return IGNORE_CHANNEL.equals(id) ? null : id;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MTU155ChannelConfig: ");
        for (int i = 0; i < channelIds.length; i++) {
            String id = channelIds[i];
            sb.append("[").append(i).append("] = ").append(id).append('\n');
        }
        sb.append("}\n");
        return sb.toString();
    }
}
