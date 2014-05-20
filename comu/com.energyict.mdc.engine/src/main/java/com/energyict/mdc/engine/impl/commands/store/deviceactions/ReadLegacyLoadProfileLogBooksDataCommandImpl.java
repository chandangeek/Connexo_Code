package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.PropertyDescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.ReadLegacyLoadProfileLogBooksDataCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.pluggable.MeterProtocolAdapter;
import java.util.List;

/**
 * Simple command that just reads the requested {@link com.energyict.mdc.protocol.api.device.BaseLoadProfile}s from the device.
 */
public class ReadLegacyLoadProfileLogBooksDataCommandImpl extends SimpleComCommand implements ReadLegacyLoadProfileLogBooksDataCommand {

    private LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand;

    public ReadLegacyLoadProfileLogBooksDataCommandImpl(final LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksCommand, final CommandRoot commandRoot) {
        super(commandRoot);
        this.legacyLoadProfileLogBooksCommand = legacyLoadProfileLogBooksCommand;
    }

    @Override
    public void doExecute (final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.legacyLoadProfileLogBooksCommand.addListOfCollectedDataItems(
                ((MeterProtocolAdapter) deviceProtocol).getLoadProfileLogBooksData(
                        legacyLoadProfileLogBooksCommand.getLoadProfileReaders(),
                        legacyLoadProfileLogBooksCommand.getLogBookReaders()));
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.READ_LEGACY_LOAD_PROFILE_LOGBOOKS_COMMAND;
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        this.logBookObisCodes(builder);
        this.loadProfileObisCodes(builder);
    }

    private void logBookObisCodes (DescriptionBuilder builder) {
        List<LogBookReader> logBookReaders = legacyLoadProfileLogBooksCommand.getLogBookReaders();
        if (logBookReaders != null && !logBookReaders.isEmpty()) {
            PropertyDescriptionBuilder logBookObisCodesBuilder = builder.addListProperty("logBookObisCodes");
            this.doLogBookObisCodes(logBookObisCodesBuilder);
        }
        else {
            builder.addProperty("logBookObisCodes").append("none");
        }
    }

    private void doLogBookObisCodes (PropertyDescriptionBuilder builder) {
        for (LogBookReader logBookReader : legacyLoadProfileLogBooksCommand.getLogBookReaders()) {
            builder = builder.append(logBookReader.getLogBookObisCode()).next();
        }
    }

    private void loadProfileObisCodes (DescriptionBuilder builder) {
        if (   legacyLoadProfileLogBooksCommand.getLoadProfilesTask() != null
            && legacyLoadProfileLogBooksCommand.getLoadProfilesTask().getLoadProfileTypes() != null
            && !legacyLoadProfileLogBooksCommand.getLoadProfilesTask().getLoadProfileTypes().isEmpty()) {
            PropertyDescriptionBuilder loadProfileObisCodesBuilder = builder.addListProperty("loadProfileObisCodes");
            this.doLoadProfileObisCodes(loadProfileObisCodesBuilder);
            builder.addProperty("markAsBadTime").append(this.legacyLoadProfileLogBooksCommand.getLoadProfilesTask().isMarkIntervalsAsBadTime());
            builder.addProperty("createEventsFromStatusFlag").append(this.legacyLoadProfileLogBooksCommand.getLoadProfilesTask().createMeterEventsFromStatusFlags());
        }
        else {
            builder.addProperty("loadProfileObisCodes").append("none");
        }
    }

    private void doLoadProfileObisCodes (PropertyDescriptionBuilder builder) {
        for (LoadProfileType loadProfileType : legacyLoadProfileLogBooksCommand.getLoadProfilesTask().getLoadProfileTypes()) {
            builder = builder.append(loadProfileType.getObisCode()).next();
        }
    }

}
