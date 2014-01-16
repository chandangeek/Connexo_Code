package com.energyict.mdc.protocol.pluggable.impl.adapters.smartmeterprotocol;

import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.issues.impl.IssueServiceImpl;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.exceptions.DataParseException;
import com.energyict.mdc.protocol.api.exceptions.LegacyProtocolException;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
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

    private IssueServiceImpl issueService;

    @Before
    public void initializeIssueService() {
        issueService = new IssueServiceImpl();
        issueService.setClock(new DefaultClock());
        com.energyict.mdc.issues.Bus.setIssueService(issueService);
    }

    @After
    public void cleanupIssueService() {
        com.energyict.mdc.issues.Bus.clearIssueService(issueService);
    }

    @Test
    public void fetchLoadProfileConfigurationEmptyTests() throws Exception {
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);
        assertNotNull("Should not get a null object back", smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(null));
        assertEquals("Should at least get an empty list", Collections.emptyList(), smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(null));
        assertNotNull("Should not get a null object back", smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(new ArrayList<LoadProfileReader>()));
        assertEquals("Should at least get an empty list", Collections.emptyList(), smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(new ArrayList<LoadProfileReader>()));
    }

    @Test
    public void getLoadProfileDataEmptyTest() {
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);
        assertNotNull("Should not get a null object back", smartMeterProtocolLoadProfileAdapter.getLoadProfileData(null));
        assertEquals("Should at least get an empty list", Collections.emptyList(), smartMeterProtocolLoadProfileAdapter.getLoadProfileData(null));
        assertNotNull("Should not get a null object back", smartMeterProtocolLoadProfileAdapter.getLoadProfileData(new ArrayList<LoadProfileReader>()));
        assertEquals("Should at least get an empty list", Collections.emptyList(), smartMeterProtocolLoadProfileAdapter.getLoadProfileData(new ArrayList<LoadProfileReader>()));
    }

    @Test
    public void fetchLoadProfileConfigurationSuccessTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getProfileObisCode()).thenReturn(loadProfileObisCode);
        LoadProfileConfiguration loadProfileConfiguration = mock(LoadProfileConfiguration.class);
        when(loadProfileConfiguration.getObisCode()).thenReturn(loadProfileObisCode);

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.<LoadProfileConfiguration>asList(loadProfileConfiguration));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);

        List<CollectedLoadProfileConfiguration> loadProfileConfigurations = smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(Arrays.<LoadProfileReader>asList(loadProfileReader));
        assertNotNull(loadProfileConfigurations);
        assertEquals(1, loadProfileConfigurations.size());
        assertEquals(loadProfileReader.getProfileObisCode(), loadProfileConfigurations.get(0).getObisCode());
    }

    @Test
    public void fetchLoadProfileConfigurationExceptionTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);

        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.fetchLoadProfileConfiguration(Matchers.<List<LoadProfileReader>>any())).thenThrow(new IOException("Could not fetch the DeviceLoadProfileConfiguration"));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);

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
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);
        List<CollectedLoadProfileConfiguration> collectedLoadProfileConfigurations = smartMeterProtocolLoadProfileAdapter.fetchLoadProfileConfiguration(Arrays.asList(loadProfileReader));

        assertThat(collectedLoadProfileConfigurations).hasSize(1);
        assertThat(collectedLoadProfileConfigurations.get(0).isSupportedByMeter()).isFalse();
        assertThat(collectedLoadProfileConfigurations.get(0).getIssues()).hasSize(1);
        assertThat(collectedLoadProfileConfigurations.get(0).getResultType()).isEqualTo(ResultType.DataIncomplete);
    }

    @Test
    public void getProfileDataWithNullListTest(){
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(null);
        final ProfileData profileData = smartMeterProtocolLoadProfileAdapter.getProfileDataWithLoadProfileId(null, 1);
        assertEquals(SmartMeterProtocolLoadProfileAdapter.INVALID_PROFILE_DATA, profileData);
    }

    @Test
    public void getProfileDataWithNotFoundTest(){
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(null);
        final ProfileData profileData = smartMeterProtocolLoadProfileAdapter.getProfileDataWithLoadProfileId(new ArrayList<ProfileData>(), 1);
        assertEquals(SmartMeterProtocolLoadProfileAdapter.INVALID_PROFILE_DATA, profileData);
    }

    @Test
    public void getProfileDataWithLoadProfileIdFoundTest(){
        ProfileData pd1 = new ProfileData(1);
        ProfileData pd2 = new ProfileData(32);
        ProfileData pd3 = new ProfileData(15);
        ProfileData pd4 = new ProfileData(54);
        List<ProfileData> profileDataList = Arrays.asList(pd1, pd2, pd3, pd4);
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(null);
        final ProfileData profileData = smartMeterProtocolLoadProfileAdapter.getProfileDataWithLoadProfileId(profileDataList, 15);
        assertEquals(pd3, profileData);
    }

    @Test
    public void getLoadProfileDataSuccessTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        when(loadProfileReader.getLoadProfileId()).thenReturn(23);
        ProfileData profileData = mock(ProfileData.class);
        when(profileData.getLoadProfileId()).thenReturn(23);
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenReturn(Arrays.asList(profileData));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);
        final List<CollectedLoadProfile> loadProfileData = smartMeterProtocolLoadProfileAdapter.getLoadProfileData(Arrays.asList(loadProfileReader));
        assertNotNull(loadProfileData);
        assertEquals(1, loadProfileData.size());
        assertEquals(ResultType.Supported, loadProfileData.get(0).getResultType());
        assertNotNull(loadProfileData.get(0).getChannelInfo());
        assertNotNull(loadProfileData.get(0).getCollectedIntervalData());
    }

    @Test
    public void getMultipleLoadProfilesWithOneFailureTest() throws IOException {
        final int loadProfileId1 = 23;
        final int loadProfileId2 = 56;
        final int loadProfileId3 = 99;
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
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);
        final List<CollectedLoadProfile> loadProfileData = smartMeterProtocolLoadProfileAdapter.getLoadProfileData(Arrays.asList(loadProfileReader1, loadProfileReader2, loadProfileReader3));
        assertEquals(3, loadProfileData.size());
        assertEquals(ResultType.NotSupported, loadProfileData.get(1).getResultType());
    }

    @Test (expected = LegacyProtocolException.class)
    public void getLoadProfilesExceptionTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenThrow(new IOException("Some exception while reading the loadProfile"));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);
        smartMeterProtocolLoadProfileAdapter.getLoadProfileData(Arrays.asList(loadProfileReader));
    }

    @Test (expected = DataParseException.class)
    public void getLoadProfileIndexOutOfBoundsTest() throws IOException {
        LoadProfileReader loadProfileReader = mock(LoadProfileReader.class);
        SmartMeterProtocol smartMeterProtocol = mock(SmartMeterProtocol.class);
        when(smartMeterProtocol.getLoadProfileData(Matchers.<List<LoadProfileReader>>any())).thenThrow(new IndexOutOfBoundsException("Requested something out of my range"));
        SmartMeterProtocolLoadProfileAdapter smartMeterProtocolLoadProfileAdapter = new SmartMeterProtocolLoadProfileAdapter(smartMeterProtocol);
        smartMeterProtocolLoadProfileAdapter.getLoadProfileData(Arrays.asList(loadProfileReader));
    }
}
