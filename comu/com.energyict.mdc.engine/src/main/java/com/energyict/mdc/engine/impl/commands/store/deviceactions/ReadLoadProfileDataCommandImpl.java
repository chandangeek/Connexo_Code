package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLoadProfileDataCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple command that just reads the requested {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile loadProfiles} from the device
 */
public class ReadLoadProfileDataCommandImpl extends SimpleComCommand implements ReadLoadProfileDataCommand {

    private LoadProfileCommand loadProfileCommand;
    private List<LoadProfileReader> loadProfileReaders = new ArrayList<>(0);

    public ReadLoadProfileDataCommandImpl(final LoadProfileCommand loadProfileCommand, final CommandRoot commandRoot) {
        super(commandRoot);
        this.loadProfileCommand = loadProfileCommand;
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        this.loadProfileReaders = loadProfileCommand.getLoadProfileReaders();
        this.loadProfileCommand.addListOfCollectedDataItems(deviceProtocol.getLoadProfileData(this.loadProfileReaders));
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (!loadProfileCommand.getLoadProfilesTask().getLoadProfileTypes().isEmpty()) {
            PropertyDescriptionBuilder loadProfileObisCodesBuilder = builder.addListProperty("loadProfileObisCodes");
            for (LoadProfileType loadProfileType : loadProfileCommand.getLoadProfilesTask().getLoadProfileTypes()) {
                loadProfileObisCodesBuilder = loadProfileObisCodesBuilder.append(loadProfileType.getObisCode()).next();
            }
            this.appendLoadProfileReaders(builder);
        }
        else {
            this.appendLoadProfileReaders(builder);
        }
    }

    private void appendLoadProfileReaders (DescriptionBuilder builder) {
        if (this.loadProfileReaders.isEmpty()) {
            builder.addLabel("There are no read profiles do read");
        }
        else {
            PropertyDescriptionBuilder loadProfilesToReadBuilder = builder.addListProperty("loadProfileToRead");
            for (LoadProfileReader loadProfileReader : this.loadProfileReaders) {
                loadProfilesToReadBuilder.append(
                        MessageFormat.format(
                                "{0} ({1,date,yyyy-MM-dd HH:mm:ss} - {2,date,yyy-MM-dd HH:mm:ss}",
                                loadProfileReader.getProfileObisCode(),
                                loadProfileReader.getStartReadingTime(),
                                loadProfileReader.getEndReadingTime()));
                loadProfilesToReadBuilder = loadProfilesToReadBuilder.next();
            }
        }
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.READ_LOAD_PROFILE_COMMAND;
    }

    @Override
    protected LogLevel defaultJournalingLogLevel () {
        return LogLevel.INFO;
    }

}