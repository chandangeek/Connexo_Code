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

    /**
     * 
     * @param channelConfig
     */
    public MTU155ChannelConfig(String channelConfig) {
        this.channelIds = channelConfig.split(":");
        this.numberOfChannels = channelIds.length;
    }

    /**
     *
     * @return
     */
    public int getNumberOfChannels() {
        return numberOfChannels;
    }

    /**
     *
     * @return
     */
    public String[] getChannelIds() {
        return channelIds;
    }

    /**
     *
     * @param channelIndex
     * @return
     */
    public String getChannelObjectId(int channelIndex) {
        if ((channelIndex >= 0) && (getNumberOfChannels() > channelIndex)) {
            String id = channelIds[channelIndex];
            return IGNORE_CHANNEL.equals(id) ? null : id;
        } else {
            return null;
        }
    }

    /**
     *
     * @param objectId
     * @return
     */
    public int getChannelId(String objectId) {
        if (objectId != null) {
            objectId = objectId.replace(" ", "");
            for (int i = 0; i < channelIds.length; i++) {
                String id = channelIds[i];
                if (id.equalsIgnoreCase(objectId)) {
                    return i;
                }
            }
        }
        return -1;
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
