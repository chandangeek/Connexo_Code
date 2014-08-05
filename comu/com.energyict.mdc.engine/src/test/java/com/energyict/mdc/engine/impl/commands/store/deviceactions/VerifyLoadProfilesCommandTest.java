package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.tasks.LoadProfilesTask;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the {@link VerifyLoadProfilesCommandImpl} component.
 *
 * @author gna
 * @since 22/05/12 - 14:46
 */
@RunWith(MockitoJUnitRunner.class)
public class VerifyLoadProfilesCommandTest extends CommonCommandImplTests {

    private static final ObisCode LoadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode ChannelInfoObisCode1 = ObisCode.fromString("1.0.1.8.0.255");
    private static final ObisCode ChannelInfoObisCode2 = ObisCode.fromString("1.0.2.8.0.255");
    private static final String MeterSerialNumber = "MeterSerialNumber";
    private static final int ProfileIntervalInSeconds = 900;
    private static final boolean FailIfConfigurationMisMatch = true;

    @Test
    public void getCorrectCommandTypeTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, mockCommandRoot());
        assertEquals(ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND, verifyLoadProfilesCommand.getCommandType());
    }

    @Test
    public void verifyNumberOfChannelsEqualsTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, mockCommandRoot());
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getNumberOfChannels()).thenReturn(2);
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);
        verifyLoadProfilesCommand.verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be no issues logged", 0, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be no problems logged", 0, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should be empty", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void verifyNumberOfChannelsNotEqualsTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, mockCommandRoot());
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getNumberOfChannels()).thenReturn(3);
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);
        verifyLoadProfilesCommand.verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be one issues logged", 1, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be one problems logged", 1, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should contain 1 element", 1, verifyLoadProfilesCommand.getReadersToRemove().size());
        assertEquals("The mock reader should be in the remove-list", loadProfileReader, verifyLoadProfilesCommand.getReadersToRemove().get(0));
    }

    @Test
    public void verifyNumberOfChannelsNotEqualsButDoNotFailTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(false);

        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, mockCommandRoot());
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getNumberOfChannels()).thenReturn(3);
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);
        verifyLoadProfilesCommand.verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("No issues should be logged, cause set to NOT fail if load profile configuration mismatch", 0, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be no problems logged", 0, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should contain NO elements", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void getLoadProfileReaderForGivenLoadProfileConfigurationNullTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Collections.<LoadProfileReader>emptyList());
        CommandRoot commandRoot = mockCommandRoot();
        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, commandRoot);

        //asserts
        assertNull(verifyLoadProfilesCommand.getLoadProfileReaderForGivenLoadProfileConfiguration(null));
    }

    @Test
    public void getLoadProfileReaderForGivenLoadProfileConfigurationNotNullTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getProfileObisCode()).thenReturn(ObisCode.fromString("0.0.0.0.0.0"));
        LoadProfileReader loadProfileReader2 = mock(LoadProfileReader.class);
        when(loadProfileReader2.getProfileObisCode()).thenReturn(LoadProfileObisCode);
        when(loadProfileReader2.getDeviceIdentifier()).thenReturn(new TestSerialNumberDeviceIdentifier(MeterSerialNumber));

        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Arrays.asList(loadProfileReader, loadProfileReader2));
        CommandRoot commandRoot = this.mockCommandRoot();
        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, commandRoot);

        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);
        when(loadProfileConfiguration.getDeviceIdentifier()).thenReturn(new TestSerialNumberDeviceIdentifier(MeterSerialNumber));

        //asserts
        assertNotNull(verifyLoadProfilesCommand.getLoadProfileReaderForGivenLoadProfileConfiguration(loadProfileConfiguration));
    }

    @Test
    public void verifyProfileIntervalCorrectTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();

        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        when(loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(Matchers.<LoadProfileReader>any())).thenReturn(ProfileIntervalInSeconds);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, this.mockCommandRoot());

        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);
        when(loadProfileConfiguration.getDeviceIdentifier()).thenReturn(new TestSerialNumberDeviceIdentifier(MeterSerialNumber));
        when(loadProfileConfiguration.getProfileInterval()).thenReturn(ProfileIntervalInSeconds);

        verifyLoadProfilesCommand.verifyProfileInterval(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be no issues logged", 0, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be no problems logged", 0, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should be empty", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void verifyProfileIntervalIncorrectTest() {
        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        when(loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(Matchers.<LoadProfileReader>any())).thenReturn(1);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, this.mockCommandRoot());

        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);
        when(loadProfileConfiguration.getDeviceIdentifier()).thenReturn(new TestSerialNumberDeviceIdentifier(MeterSerialNumber));
        when(loadProfileConfiguration.getProfileInterval()).thenReturn(ProfileIntervalInSeconds);

        verifyLoadProfilesCommand.verifyProfileInterval(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be one issues logged", 1, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be one problem logged", 1, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should contain 1 element", 1, verifyLoadProfilesCommand.getReadersToRemove().size());
        assertEquals("The mock reader should be in the remove-list", loadProfileReader, verifyLoadProfilesCommand.getReadersToRemove().get(0));
    }

    @Test(expected = GeneralParseException.class)
    public void verifyChannelInfoTestIOException() {
        List<ChannelInfo> listOfChannelInfos = new ArrayList<ChannelInfo>();
        ChannelInfo channelInfoMock1 = new ChannelInfo(0, "NoObisCodeValue", Unit.getUndefined());
        listOfChannelInfos.add(channelInfoMock1);
        ChannelInfo channelInfoMock2 = new ChannelInfo(1, "NoObisCodeValue", Unit.getUndefined());
        listOfChannelInfos.add(channelInfoMock2);

        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);

        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getChannelInfos()).thenReturn(listOfChannelInfos);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, this.mockCommandRoot());
        verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // should get a nice exception because the name of the ChannelInfo is not an ObisCode
    }

    @Test
    public void verifyChannelInfoNoExceptionWithUndefinedUnitTest() {
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();

        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);

        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getChannelInfos()).thenReturn(listOfChannelInfos);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, this.mockCommandRoot());
        verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be no issues logged", 0, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be no problems logged", 0, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should be empty", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void verifyChannelInfoNoExceptionWithProperUnitTest() {
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();

        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);

        List<ChannelInfo> secondListOfChannelInfos = new ArrayList<ChannelInfo>();
        secondListOfChannelInfos.add(new ChannelInfo(0, ChannelInfoObisCode1.toString(), Unit.get("Wh")));
        ChannelInfo channelInfoMock3 = new ChannelInfo(1, ChannelInfoObisCode2.toString(), Unit.get("kWh"));    // we replaced the Wh channelInfo with a kWh channelInfo
        secondListOfChannelInfos.add(channelInfoMock3);
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getChannelInfos()).thenReturn(secondListOfChannelInfos);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, this.mockCommandRoot());
        verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be no issues logged", 0, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be no problems logged", 0, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should be empty", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void verifyChannelInfoWithExceptionBecauseDifferentUnitTest() {
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();

        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);

        List<ChannelInfo> secondListOfChannelInfos = new ArrayList<ChannelInfo>();
        secondListOfChannelInfos.add(new ChannelInfo(0, ChannelInfoObisCode1.toString(), Unit.get("Wh")));
        ChannelInfo channelInfoMock3 = new ChannelInfo(1, ChannelInfoObisCode2.toString(), Unit.get("kvar"));    // we replaced the Wh channelInfo with a kvar channelInfo
        secondListOfChannelInfos.add(channelInfoMock3);
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getChannelInfos()).thenReturn(secondListOfChannelInfos);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(loadProfileCommand, this.mockCommandRoot());
        verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be one issues logged", 1, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be one problem logged", 1, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should contain 1 element", 1, verifyLoadProfilesCommand.getReadersToRemove().size());
        assertEquals("The mock reader should be in the remove-list", loadProfileReader, verifyLoadProfilesCommand.getReadersToRemove().get(0));
    }

    @Test
    public void executeWithoutViolations() {
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();

        DeviceLoadProfileConfiguration loadProfileConfiguration = new DeviceLoadProfileConfiguration(LoadProfileObisCode, new TestSerialNumberDeviceIdentifier(MeterSerialNumber), true);
        loadProfileConfiguration.setChannelInfos(listOfChannelInfos);

        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.<CollectedLoadProfileConfiguration>asList(loadProfileConfiguration));

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);

        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Arrays.asList(loadProfileReader));

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = spy(new VerifyLoadProfilesCommandImpl(loadProfileCommand, this.mockCommandRoot()));
        verifyLoadProfilesCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertEquals("There should be no issues logged", 0, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be no problems logged", 0, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should be empty", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
        verify(verifyLoadProfilesCommand).verifyNumberOfChannels(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
        verify(verifyLoadProfilesCommand).verifyProfileInterval(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
        verify(verifyLoadProfilesCommand).verifyChannelConfiguration(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
    }

    @Test
    public void executeNotSupportedByTheMeter(){

        DeviceLoadProfileConfiguration loadProfileConfiguration = new DeviceLoadProfileConfiguration(LoadProfileObisCode, new TestSerialNumberDeviceIdentifier(MeterSerialNumber), false);

        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.<CollectedLoadProfileConfiguration>asList(loadProfileConfiguration));

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();

        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        when(loadProfileCommand.getLoadProfilesTask()).thenReturn(loadProfilesTask);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Arrays.asList(loadProfileReader));

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = spy(new VerifyLoadProfilesCommandImpl(loadProfileCommand, this.mockCommandRoot()));
        verifyLoadProfilesCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertEquals("There should be one issue logged", 1, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("There should be one problem logged", 1, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("The list of readers-to-be-removed should contain 1 element", 1, verifyLoadProfilesCommand.getReadersToRemove().size());
        verify(verifyLoadProfilesCommand, times(0)).verifyNumberOfChannels(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
        verify(verifyLoadProfilesCommand, times(0)).verifyProfileInterval(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
        verify(verifyLoadProfilesCommand, times(0)).verifyChannelConfiguration(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
    }

    private CommandRoot mockCommandRoot() {
        CommandRoot commandRoot = mock(CommandRoot.class);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRootServiceProvider.issueService()).thenReturn(serviceProvider.issueService());
        when(commandRootServiceProvider.clock()).thenReturn(serviceProvider.clock());
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        return commandRoot;
    }

    private List<ChannelInfo> createSimpleChannelInfoList() {
        List<ChannelInfo> listOfChannelInfos = new ArrayList<ChannelInfo>();
        ChannelInfo channelInfoMock1 = new ChannelInfo(0, ChannelInfoObisCode1.toString(), Unit.get("Wh"));
        listOfChannelInfos.add(channelInfoMock1);
        ChannelInfo channelInfoMock2 = new ChannelInfo(1, ChannelInfoObisCode2.toString(), Unit.get("Wh"));
        listOfChannelInfos.add(channelInfoMock2);
        return listOfChannelInfos;
    }

    private LoadProfilesTask createSimpleLoadProfilesTask() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(FailIfConfigurationMisMatch);
        return loadProfilesTask;
    }

    private LoadProfileReader createSimpleLoadProfileReader() {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getProfileObisCode()).thenReturn(LoadProfileObisCode);
        when(loadProfileReader.getDeviceIdentifier()).thenReturn(new TestSerialNumberDeviceIdentifier(MeterSerialNumber));
        return loadProfileReader;
    }
}