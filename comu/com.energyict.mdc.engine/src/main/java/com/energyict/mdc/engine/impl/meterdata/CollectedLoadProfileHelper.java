package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.LoadProfileReader;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public final class CollectedLoadProfileHelper {

    private static final Logger logger = Logger.getLogger(CollectedLoadProfileHelper.class.getName());

    /**
     * Used by the load profile commands to remove any channel intervals (and channel infos) from the collected LP that were not requested by the LP reader.
     * This can be the case for protocols that do not have selective access yet based for channels.
     */
    public static void removeUnwantedChannels(List<LoadProfileReader> loadProfileReaders, List<CollectedData> collectedDatas) {
        for (LoadProfileReader loadProfileReader : loadProfileReaders) {
            for (CollectedData collectedData : collectedDatas) {
                if (collectedData instanceof CollectedLoadProfile) {
                    CollectedLoadProfile collectedLoadProfile = (CollectedLoadProfile) collectedData;

                    if (collectedLoadProfile.getLoadProfileIdentifier().getLoadProfileObisCode().equalsIgnoreBChannel(loadProfileReader.getProfileObisCode())) {

                        //Only remove unwanted channels if the protocol generated more channel infos than the number of channels configured in EIServer
                        if (collectedLoadProfile.getChannelInfo().size() > loadProfileReader.getChannelInfos().size()) {

                            int index = 0;
                            Iterator<ChannelInfo> channelInfoIterator = collectedLoadProfile.getChannelInfo().iterator();
                            while (channelInfoIterator.hasNext()) {
                                ChannelInfo readChannel = channelInfoIterator.next();
                                if (!isChannelConfigured(loadProfileReader, readChannel)) {
                                    //Remove channel data that was not requested
                                    channelInfoIterator.remove();
                                    for (IntervalData intervalData : collectedLoadProfile.getCollectedIntervalData()) {
                                        if (intervalData.getIntervalValues().size() > index) {
                                            intervalData.getIntervalValues().remove(index);
                                        } else {
                                            final String errorMessage = "Error removing unwanted channel: interval values < number of channels. " +
                                                    "Index = " + index + ", Interval values size =  " + intervalData.getIntervalValues().size() +
                                                    ", Channel Info size = " + collectedLoadProfile.getChannelInfo().size() +
                                                    ". Editing existing stored load profile channels can maybe cause this, you should create a new load profile for different channel numbers.";
                                            logger.severe(errorMessage);
                                            throw new ApplicationException(errorMessage);
                                        }
                                    }
                                } else {
                                    index++;
                                }
                            }

                            for (int channelIndex = 0; channelIndex < collectedLoadProfile.getChannelInfo().size(); channelIndex++) {
                                collectedLoadProfile.getChannelInfo().get(channelIndex).setId(channelIndex);
                                collectedLoadProfile.getChannelInfo().get(channelIndex).setChannelId(channelIndex);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Fill in the proper readingTypeMRID on the collected channel infos.
     */
    public static void addReadingTypesToChannelInfos(List<CollectedData> collectedDatas, List<LoadProfileReader> loadProfileReaders) {
        for (CollectedData collectedData : collectedDatas) {
            if (collectedData instanceof CollectedLoadProfile) {
                CollectedLoadProfile collectedLoadProfile = (CollectedLoadProfile) collectedData;
                for (LoadProfileReader loadProfileReader : loadProfileReaders) {
                    addReadingTypeToChannelInfo(collectedLoadProfile, loadProfileReader);
                }
            }
        }
    }

    public static void addReadingTypeToChannelInfo(CollectedLoadProfile collectedLoadProfile, LoadProfileReader loadProfileReader) {
        if (collectedLoadProfile.getLoadProfileIdentifier().getLoadProfileObisCode().equalsIgnoreBChannel(loadProfileReader.getProfileObisCode())) {
            for (ChannelInfo collectedChannelInfo : collectedLoadProfile.getChannelInfo()) {
                Optional<ChannelInfo> configuredChannelInfo = loadProfileReader.getChannelInfos()
                        .stream()
                        .filter(configuredChannel -> configuredChannel.equals(collectedChannelInfo))
                        .findAny();
                configuredChannelInfo.ifPresent(configuredChannel -> collectedChannelInfo.setReadingTypeMRID(configuredChannel.getReadingTypeMRID()));
            }
        }
    }

    /**
     * Return true if the read out channel (identified by obiscode, unit, serial number) is also configured in EIServer.
     * Otherwise, interval data for this channel cannot be stored in EIServer. It will be filtered out.
     * <p>
     * Note that there's a special case here: if the read out channel has the same obiscode and serial number,
     * but a flow unit instead of a configured volume unit (e.g. meter channel is kWh instead of configured kW),
     * the collected interval data for that channel should still be stored, after it has been converted.
     * This conversion is done in the EIServer storer class.
     * The other direction (meter channel kW and configured in EIServer as kWh) is also supported.
     */
    private static boolean isChannelConfigured(LoadProfileReader loadProfileReader, ChannelInfo readChannel) {
        //Clone the argument
        ChannelInfo clone = new ChannelInfo(readChannel.getId(), readChannel.getName(), readChannel.getUnit(), readChannel.getMeterIdentifier(), readChannel.isCumulative(), readChannel.getReadingTypeMRID());

        //We found an exact match, cool.
        if (loadProfileReader.getChannelInfos().contains(clone)) {
            return true;
        }

        //Check if we find a match if we change the received flow unit to its volume unit counter part.
        if (clone.getUnit().isFlowUnit() && clone.getUnit().getVolumeUnit() != null) {
            clone.setUnit(clone.getUnit().getVolumeUnit());
            return loadProfileReader.getChannelInfos().contains(clone);
        }

        //Check if we find a match if we change the received volume unit to its flow unit counter part.
        if (clone.getUnit().isVolumeUnit() && clone.getUnit().getFlowUnit() != null) {
            clone.setUnit(clone.getUnit().getFlowUnit());
            return loadProfileReader.getChannelInfos().contains(clone);
        }

        return false;
    }

}
