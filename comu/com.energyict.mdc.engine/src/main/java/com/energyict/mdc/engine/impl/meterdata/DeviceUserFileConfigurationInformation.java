package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.comserver.commands.DeviceCommand;
import com.energyict.comserver.commands.StoreConfigurationUserFile;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.device.data.CollectedConfigurationInformation;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

/**
 * Provides an implementation for the {@link CollectedConfigurationInformation}
 * that keeps track of the information in a {@link UserFile}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (15:44)
 */
public class DeviceUserFileConfigurationInformation extends CollectedDeviceData implements CollectedConfigurationInformation {

    private DeviceIdentifier deviceIdentifier;
    private String fileExtension;
    private byte[] contents;

    public DeviceUserFileConfigurationInformation (DeviceIdentifier deviceIdentifier, String fileExtension, byte[] contents) {
        super();
        this.deviceIdentifier = deviceIdentifier;
        this.fileExtension = fileExtension;
        this.contents = contents;
    }

    @Override
    public boolean isConfiguredIn (DataCollectionConfiguration configuration) {
        return false;
    }

    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new StoreConfigurationUserFile(this, issueService);
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
    public byte[] getContents () {
        return this.contents;
    }

}