package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.tasks.LoadProfilesTask;
import org.junit.Test;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 9/2/13
 * Time: 1:28 PM
 */
public class ReadLegacyLoadProfileLogBooksDataCommandImplTest extends CommonCommandImplTests {

    @Test
    public void toStringNullPointerWithNothingTest() {
        LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksDataCommand = mock(LegacyLoadProfileLogBooksCommand.class);
        ReadLegacyLoadProfileLogBooksDataCommandImpl readLegacyLoadProfileLogBooksDataCommand
                = new ReadLegacyLoadProfileLogBooksDataCommandImpl(legacyLoadProfileLogBooksDataCommand, createCommandRoot());

        assertThat(readLegacyLoadProfileLogBooksDataCommand.toJournalMessageDescription(LogLevel.ERROR)).isEqualTo("ReadLegacyLoadProfileLogBooksDataCommandImpl {logBookObisCodes: none; loadProfileObisCodes: none}");
    }

    @Test
    public void toStringNullPointerWithOnlyLogbooksTest() {
        LogBookReader logBookReader = mock(LogBookReader.class);
        String logbookObisCode = "1.0.98.99.1.255";
        when(logBookReader.getLogBookObisCode()).thenReturn(ObisCode.fromString(logbookObisCode));
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksDataCommand = mock(LegacyLoadProfileLogBooksCommand.class);
        when(legacyLoadProfileLogBooksDataCommand.getLogBookReaders()).thenReturn(Arrays.asList(logBookReader));
        when(legacyLoadProfileLogBooksDataCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        ReadLegacyLoadProfileLogBooksDataCommandImpl readLegacyLoadProfileLogBooksDataCommand
                = new ReadLegacyLoadProfileLogBooksDataCommandImpl(legacyLoadProfileLogBooksDataCommand, createCommandRoot());

        assertThat(readLegacyLoadProfileLogBooksDataCommand.toJournalMessageDescription(LogLevel.ERROR)).isEqualTo("ReadLegacyLoadProfileLogBooksDataCommandImpl {logBookObisCodes: " + logbookObisCode + "; loadProfileObisCodes: none}");
    }

    @Test
    public void toStringNullPointerWithOnlyLoadProfilesTest() {
        String loadProfileObisCode = "1.0.99.1.0.255";
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(ObisCode.fromString(loadProfileObisCode));
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksDataCommand = mock(LegacyLoadProfileLogBooksCommand.class);
        when(legacyLoadProfileLogBooksDataCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        ReadLegacyLoadProfileLogBooksDataCommandImpl readLegacyLoadProfileLogBooksDataCommand
                = new ReadLegacyLoadProfileLogBooksDataCommandImpl(legacyLoadProfileLogBooksDataCommand, createCommandRoot());

        assertThat(readLegacyLoadProfileLogBooksDataCommand.toJournalMessageDescription(LogLevel.ERROR)).isEqualTo("ReadLegacyLoadProfileLogBooksDataCommandImpl {logBookObisCodes: none; loadProfileObisCodes: " + loadProfileObisCode + "; markAsBadTime: false; createEventsFromStatusFlag: false}");

    }

}
