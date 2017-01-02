package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.events.datastorage.StoreConfigurationEvent;
import com.energyict.mdc.engine.impl.meterdata.DeviceUserFileConfigurationInformation;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link DeviceCommand} interface
 * that will store configuration information of a {@link com.energyict.mdc.upl.meterdata.Device device}
 * in a UserFile alongside that device, i.e. the UserFile will be stored in the same parent folder.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-16 (16:31)
 */
public class StoreConfigurationUserFile extends DeviceCommandImpl<StoreConfigurationEvent> {

    public static final String DESCRIPTION_TITLE = "Store configuration file";

    private DeviceIdentifier deviceIdentifier;
    private String fileExtension;
    private byte[] contents;

    public StoreConfigurationUserFile(DeviceUserFileConfigurationInformation configurationInformation, ComTaskExecution comTaskExecution, ServiceProvider serviceProvider) {
        super(comTaskExecution, serviceProvider);
        this.deviceIdentifier = configurationInformation.getDeviceIdentifier();
        this.fileExtension = configurationInformation.getFileExtension();
        this.contents = configurationInformation.getContents();
    }

    @Override
    public void doExecute(ComServerDAO comServerDAO) {
        comServerDAO.storeConfigurationFile(this.deviceIdentifier, DateTimeFormatter.ISO_DATE_TIME, this.fileExtension, this.contents);
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

    protected Optional<StoreConfigurationEvent> newEvent(List<Issue> issues) {
        StoreConfigurationEvent event  =  new StoreConfigurationEvent(new ComServerEventServiceProvider(), this.deviceIdentifier);
        event.addIssues(issues);
        return Optional.of(event);
    }

    @Override
    public String getDescriptionTitle() {
        return DESCRIPTION_TITLE;
    }

}