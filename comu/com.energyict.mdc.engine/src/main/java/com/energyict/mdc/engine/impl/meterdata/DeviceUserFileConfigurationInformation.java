/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.StoreConfigurationUserFile;
import com.energyict.mdc.upl.meterdata.CollectedConfigurationInformation;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

/**
 * Provides an implementation for the {@link CollectedConfigurationInformation}
 * that keeps track of the information in a UserFile.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:44)
 */
public class DeviceUserFileConfigurationInformation extends CollectedDeviceData implements CollectedConfigurationInformation {

    private DeviceIdentifier deviceIdentifier;
    private String fileName;
    private String fileExtension;
    private byte[] contents;

    public DeviceUserFileConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileName, String fileExtension, byte[] contents) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.contents = contents;
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return false;
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new StoreConfigurationUserFile(this, this.getComTaskExecution(), serviceProvider);
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier () {
        return this.deviceIdentifier;
    }

    @Override
    public String getFileExtension () {
        return this.fileExtension;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public byte[] getContents () {
        return this.contents;
    }

}