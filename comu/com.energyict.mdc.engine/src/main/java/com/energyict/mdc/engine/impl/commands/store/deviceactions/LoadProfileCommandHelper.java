package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfileChannel;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Helper class containing common methods that can be re-used for LoadProfileBookCommand and
 * LegacyLoadProfileLogBooksCommand
 *
 * @author sva
 * @since 10/06/2015 - 17:35
 */
public class LoadProfileCommandHelper {

    /**
     * Create LoadProfileReaders for this LoadProfileCommand, based on the {@link LoadProfileType}s specified in the {@link LoadProfilesTask}.
     * If no types are specified, then a {@link LoadProfileReader} for all
     * of the {@link LoadProfile}s of the device will be created.
     */
    public static void createLoadProfileReaders(CommandRoot.ServiceProvider serviceProvider, Map<LoadProfileReader, OfflineLoadProfile> loadProfileReaderMap, LoadProfilesTask loadProfilesTask, OfflineDevice device, ComTaskExecution comTaskExecution) {
        List<OfflineLoadProfile> listOfAllLoadProfiles = device.getAllOfflineLoadProfiles();
        if (loadProfilesTask.getLoadProfileTypes().isEmpty()) {
            for (OfflineLoadProfile loadProfile : listOfAllLoadProfiles) {
                addLoadProfileToReaderList(serviceProvider, loadProfileReaderMap, loadProfile, comTaskExecution);
            }
        } else {  // Read out the specified load profile types
            for (LoadProfileType lpt : loadProfilesTask.getLoadProfileTypes()) {
                listOfAllLoadProfiles.stream().filter(loadProfile -> lpt.getId() == loadProfile.getLoadProfileTypeId()).forEach(loadProfile -> {
                    addLoadProfileToReaderList(serviceProvider, loadProfileReaderMap, loadProfile, comTaskExecution);
                });
            }
        }
    }

    /**
     * Add the given {@link LoadProfile} to the loadProfileReaderMap
     * Note that only the channels of the relevant device will be added.
     * In case of a combined load profile for a master/slave setup, it is possible to read out the different load profile "parts" separately.
     *
     * @param loadProfile the loadProfile to add
     */
    private static void addLoadProfileToReaderList(CommandRoot.ServiceProvider serviceProvider, Map<LoadProfileReader, OfflineLoadProfile> loadProfileReaderMap, OfflineLoadProfile loadProfile, ComTaskExecution comTaskExecution) {
        List<ChannelInfo> channelInfos = createChannelInfos(loadProfile, comTaskExecution);
        if (!channelInfos.isEmpty()) {
            LoadProfileReader loadProfileReader = new LoadProfileReader(
                    loadProfile.getObisCode(),
                    loadProfile.getLastReading(),
                    null,
                    (int) loadProfile.getLoadProfileId(),
                    loadProfile.getMasterSerialNumber(),
                    channelInfos);
            if (!loadProfileReaderMap.containsValue(loadProfile)) {
                loadProfileReaderMap.put(loadProfileReader, loadProfile);
            }
        }
    }

    /**
     * Create a <CODE>List</CODE> of <CODE>ChannelInfos</CODE> for the given <CODE>LoadProfile</CODE>.
     *
     * @param offlineLoadProfile the given <CODE>LoadProfile</CODE>
     * @return the new List
     */
    protected static List<ChannelInfo> createChannelInfos(final OfflineLoadProfile offlineLoadProfile, ComTaskExecution comTaskExecution) {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        //Only the channels of the actual device. This is relevant for master/slave setup with 'combined' load profiles
        offlineLoadProfile.getOfflineChannels().stream().filter(lpChannel -> lpChannel.isStoreData() && comTaskExecution.getDevice().getId() == lpChannel.getDeviceId()).forEach(lpChannel -> {
            //Only the channels of the actual device. This is relevant for master/slave setup with 'combined' load profiles
            channelInfos.add(new ChannelInfo(
                    channelInfos.size(),
                    lpChannel.getObisCode().toString(),
                    lpChannel.getUnit(),
                    getMasterDeviceIdentifier(lpChannel, offlineLoadProfile),
                    lpChannel.getReadingTypeMRID()
            ));
        });
        return channelInfos;
    }

    /**
     * In case no serialNumber is provided, we take the identifier of the loadProfile (which will most likely be the MRID)
     *
     * @param lpChannel          the offlineLoadProfileChannel
     * @param offlineLoadProfile the offlineLoadProfile
     * @return the masterIdentifier
     */
    private static String getMasterDeviceIdentifier(OfflineLoadProfileChannel lpChannel, OfflineLoadProfile offlineLoadProfile) {
        if (lpChannel.getMasterSerialNumber() == null || lpChannel.getMasterSerialNumber().isEmpty()) {
            return offlineLoadProfile.getDeviceIdentifier().toString();
        } else {
            return lpChannel.getMasterSerialNumber();
        }
    }
}