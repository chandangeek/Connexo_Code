package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.energyict.mdc.common.ApplicationContext;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks.MockCollectedLoadProfile;
import com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol.mocks.MockCollectedLoadProfileConfiguration;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link SmartMeterProtocolLoadProfileAdapter}
 *
 * @author gna
 * @since 5/04/12 - 14:04
 */
@RunWith(MockitoJUnitRunner.class)
public class SmartMeterProtocolLoadProfileAdapterTest {

    private static final ObisCode loadProfileObisCode = ObisCode.fromString("1.0.99.1.0.255");

    @Mock
    private IssueService issueService;
    @Mock
    private Environment environment;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private CollectedDataFactory collectedDataFactory;

    @Before
    public void initializeMocks () {
        when(this.collectedDataFactory.createCollectedLoadProfile(any(LoadProfileIdentifier.class))).
                thenAnswer(new Answer<CollectedLoadProfile>() {
                    @Override
                    public CollectedLoadProfile answer(InvocationOnMock invocationOnMock) throws Throwable {
                        LoadProfileIdentifier loadProfileIdentifier = (LoadProfileIdentifier) invocationOnMock.getArguments()[0];
                        MockCollectedLoadProfile collectedLoadProfile = new MockCollectedLoadProfile(loadProfileIdentifier);
                        collectedLoadProfile.setResultType(ResultType.Supported);
                        return collectedLoadProfile;
                    }
                });
        when(this.collectedDataFactory.createCollectedLoadProfileConfiguration(any(ObisCode.class), anyString())).
                thenAnswer(new Answer<CollectedLoadProfileConfiguration>() {
                    @Override
                    public CollectedLoadProfileConfiguration answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return new MockCollectedLoadProfileConfiguration(
                                (ObisCode) invocationOnMock.getArguments()[0],
                                (String) invocationOnMock.getArguments()[1]);
                    }
                });
        when(this.collectedDataFactory.createCollectedLoadProfileConfiguration(any(ObisCode.class), anyString(), eq(true))).
                thenAnswer(new Answer<CollectedLoadProfileConfiguration>() {
                    @Override
                    public CollectedLoadProfileConfiguration answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return new MockCollectedLoadProfileConfiguration(
                                (ObisCode) invocationOnMock.getArguments()[0],
                                (String) invocationOnMock.getArguments()[1],
                                true);
                    }
                });
        when(this.collectedDataFactory.createCollectedLoadProfileConfiguration(any(ObisCode.class), anyString(), eq(false))).
                thenAnswer(new Answer<CollectedLoadProfileConfiguration>() {
                    @Override
                    public CollectedLoadProfileConfiguration answer(InvocationOnMock invocationOnMock) throws Throwable {
                        return new MockCollectedLoadProfileConfiguration(
                                (ObisCode) invocationOnMock.getArguments()[0],
                                (String) invocationOnMock.getArguments()[1],
                                false);
                    }
                });
        CollectedDataFactoryProvider collectedDataFactoryProvider = mock(CollectedDataFactoryProvider.class);
        when(collectedDataFactoryProvider.getCollectedDataFactory()).thenReturn(this.collectedDataFactory);
        CollectedDataFactoryProvider.instance.set(collectedDataFactoryProvider);
        when(this.applicationContext.getModulesImplementing(CollectedDataFactory.class)).thenReturn(Arrays.asList(this.collectedDataFactory));
        when(this.environment.getApplicationContext()).thenReturn(this.applicationContext);
        when(this.environment.getErrorMsg(anyString())).thenReturn("Error message translation missing in unit testing");
        when(this.environment.getTranslation(anyString())).thenReturn("Translation missing in unit testing");
        Environment.DEFAULT.set(this.environment);
    }

    @After
    public void resetCollectedDataFactoryProvider () {
        CollectedDataFactoryProvider.instance.set(null);
    }

    @AfterClass
    public static void cleanEnvironment () {
        Environment.DEFAULT.set(null);
    }

    @Test
    public void fetchLoadProfileConfigurationEmptyTests() throws Exception {
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);
        assertThat(smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(null)).isNotNull();
        assertThat(smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(null)).isEmpty();
        assertThat(smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(new ArrayList<LoadProfileReader>())).isNotNull();
        assertThat(smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(new ArrayList<LoadProfileReader>())).isEmpty();
    }

    @Test
    public void getLoadProfileDataEmptyTest() {
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);
        assertThat(smartMeterProtocolLoadProfileAdapter.getLoadProfileData(null)).isNotNull();
        assertThat(smartMeterProtocolLoadProfileAdapter.getLoadProfileData(null)).isEmpty();
        assertThat(smartMeterProtocolLoadProfileAdapter.getLoadProfileData(new ArrayList<LoadProfileReader>())).isNotNull();
        assertThat(smartMeterProtocolLoadProfileAdapter.getLoadProfileData(new ArrayList<LoadProfileReader>())).isEmpty();
    }

    @Test
    public void fetchLoadProfileConfigurationSuccessTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getProfileObisCode()).thenReturn(loadProfileObisCode);
        LoadProfileConfiguration loadProfileConfiguration = mock(LoadProfileConfiguration.class);
        when(loadProfileConfiguration.getObisCode()).thenReturn(loadProfileObisCode);

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.<LoadProfileConfiguration>asList(loadProfileConfiguration));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);

        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(Arrays.<LoadProfileReader>asList(loadProfileReader));
        assertThat(loadProfileConfigurations).isNotNull();
        assertThat(loadProfileConfigurations).hasSize(1);
        assertThat(loadProfileConfigurations.get(0).getObisCode()).isEqualTo(loadProfileReader.getProfileObisCode());
    }

    @Test
    public void fetchLoadProfileConfigurationExceptionTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenThrow(new IOException("Could not fetch the DeviceLoadProfileConfiguration"));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);

        List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(Arrays.asList(loadProfileReader));

        assertThat(collectedLoadProfileConfigurations).hasSize(1);
        assertThat(collectedLoadProfileConfigurations.get(0).isSupportedByMeter()).isFalse();
        assertThat(collectedLoadProfileConfigurations.get(0).getIssues()).hasSize(1);
        assertThat(collectedLoadProfileConfigurations.get(0).getResultType()).isEqualTo(ResultType.DataIncomplete);
    }

    @Test
    public void fetchLoadProfileConfigurationIndexOutOfBoundsTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenThrow(new IndexOutOfBoundsException("Requested something out of my range"));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);
        List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(Arrays.asList(loadProfileReader));

        assertThat(collectedLoadProfileConfigurations).hasSize(1);
        assertThat(collectedLoadProfileConfigurations.get(0).isSupportedByMeter()).isFalse();
        assertThat(collectedLoadProfileConfigurations.get(0).getIssues()).hasSize(1);
        assertThat(collectedLoadProfileConfigurations.get(0).getResultType()).isEqualTo(ResultType.DataIncomplete);
    }

    @Test
    public void getProfileDataWithNullListTest(){
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(null, issueService);
        final ProfileData profileData = smartMeterProtocolLoadProfileAdapter.getProfileDataWithLoadProfileId(null, 1);
        assertThat(profileData).isEqualTo(SmartMeterProtocolLoadProfileAdapter.INVALID_PROFILE_DATA);
    }

    @Test
    public void getProfileDataWithNotFoundTest(){
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(null, issueService);
        final ProfileData profileData = smartMeterProtocolLoadProfileAdapter.getProfileDataWithLoadProfileId(new ArrayList<ProfileData>(), 1);
        assertThat(profileData).isEqualTo(SmartMeterProtocolLoadProfileAdapter.INVALID_PROFILE_DATA);
    }

    @Test
    public void getProfileDataWithLoadProfileIdFoundTest(){
        ProfileData pd1 = new ProfileData(1);
        ProfileData pd2 = new ProfileData(32);
        ProfileData pd3 = new ProfileData(15);
        ProfileData pd4 = new ProfileData(54);
        List<ProfileData> profileDataList = Arrays.asList(pd1, pd2, pd3, pd4);
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(null, issueService);
        final ProfileData profileData = smartMeterProtocolLoadProfileAdapter.getProfileDataWithLoadProfileId(profileDataList, 15);
        assertThat(profileData).isEqualTo(pd3);
    }

    @Test
    public void getLoadProfileDataSuccessTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getLoadProfileId()).thenReturn(23L);
        ProfileData profileData = mock(ProfileData.class);
        when(profileData.getLoadProfileId()).thenReturn(23L);
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.asList(profileData));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);
        final List<CollectedLoadProfile> loadProfileData = smartMeterProtocolLoadProfileAdapter.getLoadProfileData(Arrays.asList(loadProfileReader));
        assertThat(loadProfileData).isNotNull();
        assertThat(loadProfileData.size()).isEqualTo(1);
        assertThat(loadProfileData.get(0).getResultType()).isEqualTo(ResultType.Supported);
        assertThat(loadProfileData.get(0).getChannelInfo()).isNotNull();
        assertThat(loadProfileData.get(0).getCollectedIntervalData()).isNotNull();
    }

    @Test
    public void getMultipleLoadProfilesWithOneFailureTest() throws IOException {
        final long loadProfileId1 = 23;
        final long loadProfileId2 = 56;
        final long loadProfileId3 = 99;
        LoadProfileReader loadProfileReader1 = mock(LoadProfileReader.class);
        when(loadProfileReader1.getLoadProfileId()).thenReturn(loadProfileId1);
        LoadProfileReader loadProfileReader2 = mock(LoadProfileReader.class);
        when(loadProfileReader2.getLoadProfileId()).thenReturn(loadProfileId2);
        LoadProfileReader loadProfileReader3 = mock(LoadProfileReader.class);
        when(loadProfileReader3.getLoadProfileId()).thenReturn(loadProfileId3);
        ProfileData profileData1 = mock(ProfileData.class);
        when(profileData1.getLoadProfileId()).thenReturn(loadProfileId1);
        ProfileData profileData2 = mock(ProfileData.class);
        when(profileData2.getLoadProfileId()).thenReturn(loadProfileId3);
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.asList(profileData1, profileData2));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);
        final List<CollectedLoadProfile> loadProfileData = smartMeterProtocolLoadProfileAdapter.getLoadProfileData(Arrays.asList(loadProfileReader1, loadProfileReader2, loadProfileReader3));
        assertThat(loadProfileData.size()).isEqualTo(3);
        assertThat(loadProfileData.get(1).getResultType()).isEqualTo(ResultType.NotSupported);
    }

    @Test (expected = LegacyProtocolException.class)
    public void getLoadProfilesExceptionTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenThrow(new IOException("Some exception while reading the loadProfile"));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);
        smartMeterProtocolLoadProfileAdapter.getLoadProfileData(Arrays.asList(loadProfileReader));
    }

    @Test (expected = DataParseException.class)
    public void getLoadProfileIndexOutOfBoundsTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenThrow(new IndexOutOfBoundsException("Requested something out of my range"));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol, issueService);
        smartMeterProtocolLoadProfileAdapter.getLoadProfileData(Arrays.asList(loadProfileReader));
    }
}
