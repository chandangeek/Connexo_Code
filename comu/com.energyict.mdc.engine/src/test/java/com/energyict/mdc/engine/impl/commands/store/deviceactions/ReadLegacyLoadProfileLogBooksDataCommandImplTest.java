package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.obis.ObisCode;
import com.energyict.mdc.engine.impl.commands.collect.LegacyLoadProfileLogBooksCommand;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfilesTaskOptions;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.commands.store.core.ComCommandDescriptionTitle;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.protocol.LogBookReader;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;
import org.junit.Test;
import org.mockito.Mock;

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

    @Mock
    private OfflineDevice offlineDevice;

    @Test
    public void toStringNullPointerWithNothingTest() {
        LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksDataCommand = mock(LegacyLoadProfileLogBooksCommand.class);
        ReadLegacyLoadProfileLogBooksDataCommandImpl readLegacyLoadProfileLogBooksDataCommand
                = new ReadLegacyLoadProfileLogBooksDataCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), legacyLoadProfileLogBooksDataCommand);

        String journalMessage = readLegacyLoadProfileLogBooksDataCommand.toJournalMessageDescription(LogLevel.ERROR);
        assertThat(journalMessage).isEqualTo(ComCommandDescriptionTitle.ReadLegacyLoadProfileLogBooksDataCommandImpl.getDescription() + " {collectedProfiles: ; collectedLogBooks: }");
    }

    @Test
    public void toStringNullPointerWithOnlyLogbooksTest() {
        LogBookReader logBookReader = mock(LogBookReader.class);
        String logbookObisCode = "1.0.98.99.1.255";
        when(logBookReader.getLogBookObisCode()).thenReturn(ObisCode.fromString(logbookObisCode));
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksDataCommand = mock(LegacyLoadProfileLogBooksCommand.class);
        when(legacyLoadProfileLogBooksDataCommand.getLogBookReaders()).thenReturn(Arrays.asList(logBookReader));
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(legacyLoadProfileLogBooksDataCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        ReadLegacyLoadProfileLogBooksDataCommandImpl readLegacyLoadProfileLogBooksDataCommand
                = new ReadLegacyLoadProfileLogBooksDataCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), legacyLoadProfileLogBooksDataCommand);

        String journalMessage = readLegacyLoadProfileLogBooksDataCommand.toJournalMessageDescription(LogLevel.ERROR);
        assertThat(journalMessage).isEqualTo(ComCommandDescriptionTitle.ReadLegacyLoadProfileLogBooksDataCommandImpl.getDescription() + " {collectedProfiles: ; collectedLogBooks: (1.0.98.99.1.255)}");
    }

    @Test
    public void toStringNullPointerWithOnlyLoadProfilesTest() {
        String loadProfileObisCode = "1.0.99.1.0.255";
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getObisCode()).thenReturn(ObisCode.fromString(loadProfileObisCode));
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.getLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType));
        LegacyLoadProfileLogBooksCommand legacyLoadProfileLogBooksDataCommand = mock(LegacyLoadProfileLogBooksCommand.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(legacyLoadProfileLogBooksDataCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        ReadLegacyLoadProfileLogBooksDataCommandImpl readLegacyLoadProfileLogBooksDataCommand
                = new ReadLegacyLoadProfileLogBooksDataCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), legacyLoadProfileLogBooksDataCommand);

        String journalMessage = readLegacyLoadProfileLogBooksDataCommand.toJournalMessageDescription(LogLevel.ERROR);
        assertThat(journalMessage).isEqualTo(ComCommandDescriptionTitle.ReadLegacyLoadProfileLogBooksDataCommandImpl.getDescription() + " {collectedProfiles: ; collectedLogBooks: }");
    }
}