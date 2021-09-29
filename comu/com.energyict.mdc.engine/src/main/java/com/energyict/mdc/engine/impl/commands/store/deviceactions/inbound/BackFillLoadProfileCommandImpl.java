package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.LoadProfilesTask;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.protocol.LoadProfileReader;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BackFillLoadProfileCommandImpl extends LoadProfileCommandImpl {

    public BackFillLoadProfileCommandImpl(GroupedDeviceCommand groupedDeviceCommand, LoadProfilesTask loadProfilesTask, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData) {
        super(groupedDeviceCommand, loadProfilesTask, comTaskExecution);
        updateLoadProfileReaders(collectedData, groupedDeviceCommand.getCommandRoot().getServiceProvider().deviceService());
    }

    private void updateLoadProfileReaders(List<ServerCollectedData> collectedData, DeviceService deviceService) {
        Map<LoadProfileReader, OfflineLoadProfile> clonedMap = new HashMap<>(getLoadProfileReaderMap());
        getLoadProfileReaderMap().clear();
        for (LoadProfileReader loadProfileReader : clonedMap.keySet()) {
            OfflineLoadProfile offlineLoadProfile = clonedMap.get(loadProfileReader);
            collectedData.stream()
                    .filter(CollectedLoadProfile.class::isInstance)
                    .map(CollectedLoadProfile.class::cast)
                    .filter(collectedLoadProfile -> collectedLoadProfile.getLoadProfileIdentifier().getLoadProfileObisCode().equals(loadProfileReader.getProfileObisCode()))
                    .forEach(collectedLoadProfile -> addToLoadProfileReaderMapIfValid(loadProfileReader, collectedLoadProfile, offlineLoadProfile, deviceService));
        }
    }

    private void addToLoadProfileReaderMapIfValid(LoadProfileReader loadProfileReader, CollectedLoadProfile collectedLoadProfile, OfflineLoadProfile offlineLoadProfile, DeviceService deviceService) {
        Device device = deviceService.findDeviceByIdentifier(offlineLoadProfile.getDeviceIdentifier())
                .orElseThrow(() -> new NotFoundException("Could not resolve device identifier: '" + offlineLoadProfile.getDeviceIdentifier().toString() + "'"));
        Date lastConsecutiveReading = device.getLoadProfiles().stream().filter(lp -> lp.getLoadProfileTypeObisCode().equals(loadProfileReader.getProfileObisCode()))
                .findFirst().get().getLastConsecutiveReading()
                .orElseThrow(() -> new IllegalArgumentException("Last consecutive reading has not been set. Please make sure the device installation date is set."));

        LoadProfileReader newLoadProfileReader = new LoadProfileReader(loadProfileReader.getProfileObisCode()
                ,                                            lastConsecutiveReading
                ,                                            new Date(collectedLoadProfile.getCollectedIntervalDataRange().lowerEndpoint().toEpochMilli())
                ,                                            loadProfileReader.getLoadProfileId()
                ,                                            loadProfileReader.getMeterSerialNumber()
                ,                                            loadProfileReader.getChannelInfos());
        if (isValid(newLoadProfileReader)) {
            getLoadProfileReaderMap().put(newLoadProfileReader, offlineLoadProfile);
        }
    }

    private boolean isValid(LoadProfileReader reader) {
        return reader.getStartReadingTime().before(reader.getEndReadingTime());
    }
}