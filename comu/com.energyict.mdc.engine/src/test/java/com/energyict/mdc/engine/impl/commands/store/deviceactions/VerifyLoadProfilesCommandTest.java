package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.engine.TestSerialNumberDeviceIdentifier;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.common.CommonCommandImplTests;
import com.energyict.mdc.engine.impl.meterdata.DeviceLoadProfileConfiguration;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.issues.Warning;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.exceptions.GeneralParseException;
import com.energyict.mdc.tasks.LoadProfilesTask;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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
        assertThat(verifyLoadProfilesCommand.getCommandType()).isEqualTo(ComCommandTypes.VERIFY_LOAD_PROFILE_COMMAND);
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
        assertThat(verifyLoadProfilesCommand.getIssues()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getProblems()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getWarnings()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).isEmpty();
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
        List<Issue> issues = verifyLoadProfilesCommand.verifyNumberOfChannels(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertThat(issues).hasSize(1);
        assertThat(getProblems(issues)).hasSize(1);
        assertThat(getWarnings(issues)).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).hasSize(1);
        assertThat(verifyLoadProfilesCommand.getReadersToRemove().get(0)).isEqualTo(loadProfileReader);
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
        assertThat(verifyLoadProfilesCommand.getIssues()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getProblems()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getWarnings()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).isEmpty();
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
        assertThat(verifyLoadProfilesCommand.getLoadProfileReaderForGivenLoadProfileConfiguration(null)).isNull();
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
        assertThat(verifyLoadProfilesCommand.getLoadProfileReaderForGivenLoadProfileConfiguration(loadProfileConfiguration)).isNotNull();
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
        assertThat(verifyLoadProfilesCommand.getIssues()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getProblems()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getWarnings()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).isEmpty();
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

        List<Issue> issues = verifyLoadProfilesCommand.verifyProfileInterval(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertThat(issues).hasSize(1);
        assertThat(getProblems(issues)).hasSize(1);
        assertThat(getWarnings(issues)).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).hasSize(1);
        assertThat(verifyLoadProfilesCommand.getReadersToRemove().get(0)).isEqualTo(loadProfileReader);
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
        assertThat(verifyLoadProfilesCommand.getIssues()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getProblems()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getWarnings()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).isEmpty();
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
        assertThat(verifyLoadProfilesCommand.getIssues()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getProblems()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getWarnings()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).isEmpty();
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
        List<Issue> issues = verifyLoadProfilesCommand.verifyChannelConfiguration(loadProfileReader, loadProfileConfiguration);

        // asserts
        assertThat(issues).hasSize(1);
        assertThat(getProblems(issues)).hasSize(1);
        assertThat(getWarnings(issues)).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).hasSize(1);
        assertThat(verifyLoadProfilesCommand.getReadersToRemove().get(0)).isEqualTo(loadProfileReader);
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
        assertThat(verifyLoadProfilesCommand.getIssues()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getProblems()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getWarnings()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).isEmpty();
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
        assertThat(verifyLoadProfilesCommand.getIssues()).hasSize(1);
        assertThat(verifyLoadProfilesCommand.getWarnings()).isEmpty();
        assertThat(verifyLoadProfilesCommand.getProblems()).hasSize(1);
        assertThat(verifyLoadProfilesCommand.getReadersToRemove()).hasSize(1);
        verify(verifyLoadProfilesCommand, times(0)).verifyNumberOfChannels(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
        verify(verifyLoadProfilesCommand, times(0)).verifyProfileInterval(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
        verify(verifyLoadProfilesCommand, times(0)).verifyChannelConfiguration(Matchers.<LoadProfileReader>any(), Matchers.<DeviceLoadProfileConfiguration>any());
    }

    private CommandRoot mockCommandRoot() {
        IssueService issueService = executionContextServiceProvider.issueService();
        Clock clock = executionContextServiceProvider.clock();
        CommandRoot commandRoot = mock(CommandRoot.class);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit testing");
        Thesaurus thesaurus = mock(Thesaurus.class);
        when(thesaurus.getFormat(any(MessageSeed.class))).thenReturn(messageFormat);
        CommandRoot.ServiceProvider commandRootServiceProvider = mock(CommandRoot.ServiceProvider.class);
        when(commandRootServiceProvider.thesaurus()).thenReturn(thesaurus);
        when(commandRootServiceProvider.issueService()).thenReturn(issueService);
        when(commandRootServiceProvider.clock()).thenReturn(clock);
        when(commandRoot.getServiceProvider()).thenReturn(commandRootServiceProvider);
        return commandRoot;
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