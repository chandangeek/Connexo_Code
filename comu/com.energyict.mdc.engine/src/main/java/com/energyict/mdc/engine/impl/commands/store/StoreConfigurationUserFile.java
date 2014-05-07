package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.meterdata.DeviceUserFileConfigurationInformation;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;
import java.text.SimpleDateFormat;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will store configuration information of a {@link com.energyict.mdc.protocol.api.device.BaseDevice device}
 * in a {@link UserFile} alongside that device,
 * i.e. the UserFile will be stored in the same parent folder.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (16:31)
 */
public class StoreConfigurationUserFile extends DeviceCommandImpl {

    private DeviceIdentifier deviceIdentifier;
    private String fileExtension;
    private byte[] contents;

    public StoreConfigurationUserFile(DeviceUserFileConfigurationInformation configurationInformation, IssueService issueService, Clock clock) {
        super(issueService, clock);
        this.deviceIdentifier = configurationInformation.getDeviceIdentifier();
        this.fileExtension = configurationInformation.getFileExtension();
        this.contents = configurationInformation.getContents();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.storeConfigurationFile(this.deviceIdentifier, new SimpleDateFormat("yyyy_MM_dd_HH-mm"), this.fileExtension, this.contents);
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel() {
        return ComServer.LogLevel.INFO;
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("deviceIdentifier").append(this.deviceIdentifier);
        }
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.DEBUG)) {
            builder.addProperty("file extension").append(this.fileExtension);
        }
    }
}