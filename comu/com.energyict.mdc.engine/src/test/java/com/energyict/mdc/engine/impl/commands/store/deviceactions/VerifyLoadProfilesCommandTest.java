package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.cbo.Unit;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfilesTaskOptions;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.Problem;
import com.energyict.mdc.upl.issue.Warning;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private OfflineDevice offlineDevice;

    @Test
    public void getCorrectCommandTypeTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
        assertEquals(ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND, verifyLoadProfilesCommand.getCommandType());
    }

    @Test
    public void verifyNumberOfChannelsEqualsTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getNumberOfChannels()).thenReturn(2);
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);
        List<Issue> issueList = verifyLoadProfilesCommand.verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be no issues logged", 0, issueList.size());
        assertEquals("There should be no problems logged", 0, getProblems(issueList).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issueList).size());
        assertEquals("The list of readers-to-be-removed should be empty", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void verifyNumberOfChannelsNotEqualsTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getNumberOfChannels()).thenReturn(3);
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);
        List<Issue> issues = verifyLoadProfilesCommand.verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be one issues logged", 1, issues.size());
        assertEquals("There should be one problems logged", 1, getProblems(issues).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issues).size());
        assertEquals("The list of readers-to-be-removed should contain 1 element", 1, verifyLoadProfilesCommand.getReadersToRemove().size());
        assertEquals("The mock reader should be in the remove-list", loadProfileReader, verifyLoadProfilesCommand.getReadersToRemove().get(0));
    }

    @Test
    public void verifyNumberOfChannelsNotEqualsButDoNotFailTest() {
        LoadProfilesTask loadProfilesTask = mock(LoadProfilesTask.class);
        when(loadProfilesTask.failIfLoadProfileConfigurationMisMatch()).thenReturn(false);

        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getNumberOfChannels()).thenReturn(3);
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);
        List<Issue> issues = verifyLoadProfilesCommand.verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("No issues should be logged, cause set to NOT fail if load profile configuration mismatch", 0, issues.size());
        assertEquals("There should be no problems logged", 0, getProblems(issues).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issues).size());
        assertEquals("The list of readers-to-be-removed should contain NO elements", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void getLoadProfileReaderForGivenLoadProfileConfigurationNullTest() {
        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Collections.<LoadProfileReader>emptyList());
        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);

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

        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Arrays.asList(loadProfileReader, loadProfileReader2));
        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);

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
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        when(loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(Matchers.<LoadProfileReader>any())).thenReturn(ProfileIntervalInSeconds);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);

        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);
        when(loadProfileConfiguration.getDeviceIdentifier()).thenReturn(new TestSerialNumberDeviceIdentifier(MeterSerialNumber));
        when(loadProfileConfiguration.getProfileInterval()).thenReturn(ProfileIntervalInSeconds);

        List<Issue> issues = verifyLoadProfilesCommand.verifyProfileInterval(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be no issues logged", 0, issues.size());
        assertEquals("There should be no problems logged", 0, getProblems(issues).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issues).size());
        assertEquals("The list of readers-to-be-removed should be empty", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void verifyProfileIntervalIncorrectTest() {
        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        when(loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(Matchers.<LoadProfileReader>any())).thenReturn(1);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);

        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);
        when(loadProfileConfiguration.getDeviceIdentifier()).thenReturn(new TestSerialNumberDeviceIdentifier(MeterSerialNumber));
        when(loadProfileConfiguration.getProfileInterval()).thenReturn(ProfileIntervalInSeconds);

        List<Issue> issues = verifyLoadProfilesCommand.verifyProfileInterval(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be one issues logged", 1, issues.size());
        assertEquals("There should be one problem logged", 1, getProblems(issues).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issues).size());
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
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
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
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
        List<Issue> issues = verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be no issues logged", 0, issues.size());
        assertEquals("There should be no problems logged", 0, getProblems(issues).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issues).size());
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
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
        List<Issue> issues = verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be no issues logged", 0, issues.size());
        assertEquals("There should be no problems logged", 0, getProblems(issues).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issues).size());
        assertEquals("The list of readers-to-be-removed should be empty", 0, verifyLoadProfilesCommand.getReadersToRemove().size());
    }

    @Test
    public void verifyChannelInfoWithExceptionBecauseDifferentUnitTest() {
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();

        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);

        List<ChannelInfo> secondListOfChannelInfos = new ArrayList<ChannelInfo>();
        secondListOfChannelInfos.add(new ChannelInfo(0, ChannelInfoObisCode1.toString(), Unit.get("Wh")));
        secondListOfChannelInfos.add(new ChannelInfo(1, ChannelInfoObisCode2.toString(), Unit.get("kvar")));    // we replaced the Wh channelInfo with a kvar channelInfo
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getChannelInfos()).thenReturn(secondListOfChannelInfos);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
        List<Issue> issues = verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be one issues logged", 1, issues.size());
        assertEquals("There should be one problem logged", 1, getProblems(issues).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issues).size());
        assertEquals("The list of readers-to-be-removed should contain 1 element", 1, verifyLoadProfilesCommand.getReadersToRemove().size());
        assertEquals("The mock reader should be in the remove-list", loadProfileReader, verifyLoadProfilesCommand.getReadersToRemove().get(0));
    }

    @Test
    public void verifyChannelInfoWithExceptionBecauseMultipleDifferentUnitsTest() {
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();

        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);

        List<ChannelInfo> secondListOfChannelInfos = new ArrayList<ChannelInfo>();
        secondListOfChannelInfos.add(new ChannelInfo(0, ChannelInfoObisCode1.toString(), Unit.get("kvar")));      // we replaced the Wh channelInfo with a kvar channelInfo
        ChannelInfo channelInfoMock3 = new ChannelInfo(1, ChannelInfoObisCode2.toString(), Unit.get("kvar"));    // we replaced the Wh channelInfo with a kvar channelInfo
        secondListOfChannelInfos.add(channelInfoMock3);
        DeviceLoadProfileConfiguration loadProfileConfiguration = mock(DeviceLoadProfileConfiguration.class);
        when(loadProfileConfiguration.getChannelInfos()).thenReturn(secondListOfChannelInfos);
        when(loadProfileConfiguration.getObisCode()).thenReturn(LoadProfileObisCode);

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand);
        List<Issue> issues = verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertEquals("There should be two issues logged", 2, issues.size());
        assertEquals("There should be two problem logged", 2, getProblems(issues).size());
        assertEquals("There should be no warnings logged", 0, getWarnings(issues).size());
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
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Arrays.asList(loadProfileReader));

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = spy(new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand));
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
    public void executeNotSupportedByTheMeter() {

        DeviceLoadProfileConfiguration loadProfileConfiguration = new DeviceLoadProfileConfiguration(LoadProfileObisCode, new TestSerialNumberDeviceIdentifier(MeterSerialNumber), false);

        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.<CollectedLoadProfileConfiguration>asList(loadProfileConfiguration));

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();

        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Arrays.asList(loadProfileReader));

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = spy(new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand));
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

    @Test
    public void executeWithMultipleIssues() {
        List<ChannelInfo> listOfChannelInfos = createSimpleChannelInfoList();

        List<ChannelInfo> secondListOfChannelInfos = new ArrayList<ChannelInfo>();
        secondListOfChannelInfos.add(new ChannelInfo(0, ChannelInfoObisCode1.toString(), Unit.get("kvar")));    // we replaced the Wh channelInfo with a kvar channelInfo
        secondListOfChannelInfos.add(new ChannelInfo(1, ChannelInfoObisCode2.toString(), Unit.get("kvar")));    // we replaced the Wh channelInfo with a kvar channelInfo

        DeviceLoadProfileConfiguration loadProfileConfiguration = new DeviceLoadProfileConfiguration(LoadProfileObisCode, new TestSerialNumberDeviceIdentifier(MeterSerialNumber), true);
        loadProfileConfiguration.setProfileInterval(300);
        loadProfileConfiguration.setChannelInfos(secondListOfChannelInfos);

        DeviceProtocol deviceProtocol = mock(DeviceProtocol.class);
        when(deviceProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.<CollectedLoadProfileConfiguration>asList(loadProfileConfiguration));

        LoadProfilesTask loadProfilesTask = createSimpleLoadProfilesTask();
        LoadProfileReader loadProfileReader = createSimpleLoadProfileReader();
        when(loadProfileReader.getChannelInfos()).thenReturn(listOfChannelInfos);

        LoadProfileCommandImpl loadProfileCommand = mock(LoadProfileCommandImpl.class);
        LoadProfilesTaskOptions loadProfilesTaskOptions = new LoadProfilesTaskOptions(loadProfilesTask);
        when(loadProfileCommand.getLoadProfilesTaskOptions()).thenReturn(loadProfilesTaskOptions);
        when(loadProfileCommand.getLoadProfileReaders()).thenReturn(Arrays.asList(loadProfileReader));
        when(loadProfileCommand.findLoadProfileIntervalForLoadProfileReader(loadProfileReader)).thenReturn(900);

        VerifyLoadProfilesCommandImpl verifyLoadProfilesCommand = spy(new VerifyLoadProfilesCommandImpl(createGroupedDeviceCommand(offlineDevice, deviceProtocol), loadProfileCommand));
        verifyLoadProfilesCommand.execute(deviceProtocol, newTestExecutionContext());

        // asserts
        assertEquals("There should be three issues logged", 3, verifyLoadProfilesCommand.getIssues().size());
        assertEquals("There should be three problems logged", 3, verifyLoadProfilesCommand.getProblems().size());
        assertEquals("There should be no warnings logged", 0, verifyLoadProfilesCommand.getWarnings().size());
        assertEquals("The list of readers-to-be-removed should contain 1 element", 1, verifyLoadProfilesCommand.getReadersToRemove().size());
        verify(verifyLoadProfilesCommand).verifyNumberOfChannels(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
        verify(verifyLoadProfilesCommand).verifyProfileInterval(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
        verify(verifyLoadProfilesCommand).verifyChannelConfiguration(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
    }

    private List<ChannelInfo> createSimpleChannelInfoList() {
        List<ChannelInfo> listOfChannelInfos = new ArrayList<>();
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

    private List<Problem> getProblems(List<Issue> allIssues) {
        return allIssues
                .stream()
                .filter(Issue::isProblem)
                .map(Problem.class::cast)
                .collect(Collectors.toList());
    }

    private List<Warning> getWarnings(List<Issue> allIssues) {
        return allIssues
                .stream()
                .filter(Issue::isWarning)
                .map(Warning.class::cast)
                .collect(Collectors.toList());
    }

}