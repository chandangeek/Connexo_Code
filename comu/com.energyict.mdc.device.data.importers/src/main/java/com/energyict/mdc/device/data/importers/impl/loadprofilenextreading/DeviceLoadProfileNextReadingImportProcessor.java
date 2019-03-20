package com.energyict.mdc.device.data.importers.impl.loadprofilenextreading;

import com.elster.jupiter.fileimport.csvimport.exceptions.ProcessorException;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.importers.impl.AbstractDeviceDataFileImportProcessor;
import com.energyict.mdc.device.data.importers.impl.DeviceDataImporterContext;
import com.energyict.mdc.device.data.importers.impl.FileImportLogger;
import com.energyict.mdc.device.data.importers.impl.MessageSeeds;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.obis.ObisCode;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DeviceLoadProfileNextReadingImportProcessor extends AbstractDeviceDataFileImportProcessor<DeviceLoadProfileNextReadingRecord>{

    private Device device;
    public DeviceLoadProfileNextReadingImportProcessor(DeviceDataImporterContext context) { super(context);}

    @Override
    public void process(DeviceLoadProfileNextReadingRecord data, FileImportLogger logger) throws ProcessorException{
        setDevice(data,logger);
        validateDeviceState(data, device);
        Optional<LoadProfile> validLoadProfile = getLoadProfileByOBIS(device, data.getLoadProfilesOBIS());
        if ( validLoadProfile.isPresent())
            addNextBlockDateToLoadProfile(device, validLoadProfile.get(), Optional.ofNullable(data.getLoadProfileNextReadingBlockDateTime()));
        else
            throw new ProcessorException(MessageSeeds.INVALID_DEVICE_LOADPROFILE_OBIS_CODE,
                    data.getLineNumber(),data.getLoadProfilesOBIS(),device.getName());
    }

    @Override
    public void complete(FileImportLogger logger) {

    }

    private void setDevice(DeviceLoadProfileNextReadingRecord data, FileImportLogger logger) {
        if (device == null ||
                (!device.getmRID().equals(data.getDeviceIdentifier()) && !device.getName().equals(data.getDeviceIdentifier()))) {
            complete(logger);//when new identifier comes we store all previous data read
            device = findDeviceByIdentifier(data.getDeviceIdentifier())
                    .orElseThrow(() -> new ProcessorException(MessageSeeds.NO_DEVICE, data.getLineNumber(), data.getDeviceIdentifier()));
        }
        validateDeviceState(data, device);
    }

    private void validateDeviceState(DeviceLoadProfileNextReadingRecord data, Device device) {
        if (device.getState().getName().equals(DefaultState.IN_STOCK.getKey())) {
            throw new ProcessorException(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_IN_STOCK_DEVICE, data.getLineNumber(), device.getName());
        }
        if (device.getState().getName().equals(DefaultState.DECOMMISSIONED.getKey())
                && !((User) getContext().getThreadPrincipalService().getPrincipal()).hasPrivilege("MDC", Privileges.Constants.ADMINISTER_DECOMMISSIONED_DEVICE_DATA)) {
            throw new ProcessorException(MessageSeeds.READING_IMPORT_NOT_ALLOWED_FOR_DECOMMISSIONED_DEVICE, data.getLineNumber(), device.getName());
        }
    }

    // initial erau private
    public Optional<LoadProfile> getLoadProfileByOBIS(Device device, String loadProfileOBISCode){

        Optional<LoadProfile> loadProfileWithValidOBISCode = device.getLoadProfiles()
                            .stream()
                            .filter(x->x.getDeviceObisCode().equals(ObisCode.fromString(loadProfileOBISCode)))
                            .findFirst();
       return loadProfileWithValidOBISCode;
    }

    // initial erau private
     public void addNextBlockDateToLoadProfile(Device device, LoadProfile loadProfile, Optional<ZonedDateTime> nextReadingBlockDateTime){

        if (nextReadingBlockDateTime.isPresent()){
            // date is present in import file, add it to loadProfile
            LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(loadProfile);
            Instant newLastReading = nextReadingBlockDateTime.get().toInstant();
            loadProfileUpdater.setLastReading(newLastReading);
            loadProfileUpdater.update();
        }
        else
        {
            // date is absent from import file, synchronize with DateUntil
            LoadProfile.LoadProfileUpdater loadProfileUpdater = device.getLoadProfileUpdaterFor(loadProfile);

            Optional<Instant> dataUntil = loadProfile.getChannels()
                    .stream()
                    .map(c->c.getLastDateTime())
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max(Comparator.comparing(Instant::toEpochMilli));

            loadProfileUpdater.setLastReading(dataUntil.orElse(null));
            loadProfileUpdater.update();
        }

    }
}