package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Batch;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceEstimation;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LogBook;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.lifecycle.config.DefaultState;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 11/05/2016
 * Time: 9:07
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceInfoFactoryTest {

    private static final Unit kiloWattHours = Unit.get("kWh");

    private static final String READING_TYPE_MRID_1 = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String READING_TYPE_MRID_2 = "0.0.0.1.19.1.12.0.0.0.0.0.0.0.0.3.72.0";
    private static final String READING_TYPE_MRID_3 = "0.0.0.1.1.1.12.0.0.0.0.1.0.0.0.3.72.0";
    private static final String READING_TYPE_MRID_4 = "0.0.0.1.1.1.12.0.0.0.0.2.0.0.0.3.72.0";
    private static final String READING_TYPE_MRID_5 = "0.0.0.1.19.1.12.0.0.0.0.1.0.0.0.3.72.0";
    private static final String READING_TYPE_MRID_6 = "0.0.0.1.19.1.12.0.0.0.0.2.0.0.0.3.72.0";

    private static final String LOADPROFILE_TYPE_NAME_1 = "TheLoadProfileTypeName1";
    private static final String LOADPROFILE_TYPE_NAME_2 = "TheLoadProfileTypeName2";
    private static final String LOADPROFILE_TYPE_NAME_3 = "TheLoadProfileTypeName3";
    private static final String LOADPROFILE_TYPE_NAME_4 = "TheLoadProfileTypeName4";

    private static final String STATE_NAME = "dlc.default.active";
    private static final String STATE_TRANSLATION = "Active";

    private static final String USAGEPOINT_NAME = "TheUsagePoint";
    private static final String SERVICE_CATEGORY_NAME = "TheServiceCategoryName";

    private static final int ISSUE_COUNT = 2;
    private static final long ISSUE_DATA_VALIDATION_ID = 142L;
    private static final long PROTOCOL_ID = 5421L;

    private static final long DATA_LOGGER_DEVICE_TYPE_ID = 33236L;
    private static final String DATA_LOGGER_DEVICE_TYPE_NAME = "TheDataLoggerDeviceTypesName";
    private static final String SLAVE_DEVICE_TYPE_NAME_1 = "TheSlave1TypesName";
    private static final String SLAVE_DEVICE_TYPE_NAME_2 = "TheSlave2TypesName";

    private static final long DATALOGGER_DEVICE_CONFIGURATION_ID = 6586L;
    private static final String DATALOGGER_DEVICE_CONFIGURATION_NAME = "TheDeviceTypesName";
    private static final long DATALOGGER_DEVICE_CONFIGURATION_VERSION = 3L;
    private static final String SLAVE_DEVICE_CONFIGURATION_NAME_1 = "Slave1DeviceConfiguration";
    private static final String SLAVE_DEVICE_CONFIGURATION_NAME_2 = "Slave2DeviceConfiguration";

    private static final long DATALOGGER_ID = 397L;
    private static final String DATALOGGER_NAME = "TheDataLoggersName";
    private static final String DATALOGGER_SERIAL = "TheDataLoggersSerialNumber";
    private static final String DATALOGGER_MANUFACTURER = "TheDataLogger's manufacturer";
    private static final String DATALOGGER_MODELNBR = "TheDataLogger's model number";
    private static final String DATALOGGER_MODELVERSION = "TheDataLogger's model version";

    private static final int DATALOGGER_YEAR_OF_CERTIFICATION = 1960;
    private static final long DATALOGGER_VERSION = 1L;
    private static final String SLAVE_1 = "TheFirstSlave";
    private static final String SLAVE_2 = "TheSecondSlave";
    private static final String SLAVE_CHANNEL_NAME_1 = "slaveChannel1";
    private static final String SLAVE_CHANNEL_NAME_2 = "slaveChannel2";

    private static final String BATCH_NAME = "TheBatchesName";

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private IssueService issueService;
    @Mock
    private IssueStatus issueStatusOpen;
    @Mock
    private TopologyService topologyService;
    @Mock
    private IssueDataValidationService issueDataValidationService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private AmrSystem amrSystem;
    @Mock
    private BatchService batchService;
    @Mock
    DeviceDataInfoFactory deviceDataInfoFactory;
    @Mock
    private Batch batch;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;
    @Mock
    private DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService;

    @Mock
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    @Mock
    private DeviceType dataLoggerDeviceType, slaveDeviceType1, slaveDeviceType2;
    @Mock
    private DeviceConfiguration dateLoggerDeviceConfiguration, slaveDeviceConfiguration1, slaveDeviceConfiguration2;
    @Mock
    private Device dataLogger;
    @Mock
    private LoadProfileType loadProfileType1, loadProfileType2, loadProfileType3, loadProfileType4;
    @Mock
    private LoadProfileSpec loadProfileSpec1, loadProfileSpec2, loadProfileSpec3, loadProfileSpec4;
    @Mock
    private LoadProfile loadProfile1, loadProfile2, loadProfile3, loadProfile4;
    @Mock
    private Channel dataLoggerChn1, dataLoggerChn2, dataLoggerChn3, dataLoggerChn4, dataLoggerChn5, dataLoggerChn6;
    @Mock
    private ChannelSpec channelSpec;
    @Mock
    private ReadingType readingTypeForChannel1, readingTypeForChannel2, readingTypeForChannel3, readingTypeForChannel4, readingTypeForChannel5, readingTypeForChannel6;
    @Mock
    private Device slave1, slave2;
    @Mock
    private Channel slaveChn1, slaveChn2;
    @Mock
    private LogBook logBook1, logBook2, logBook3;
    @Mock
    private Meter meter;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private DeviceEstimation deviceEstimation;
    @Mock
    private State state;
    @Mock
    private DeviceService deviceService;
    private Clock clock = Clock.systemDefaultZone();
    private ChannelInfoFactory channelInfoFactory;

    @Before
    public void initMocks() {
        this.setupTranslations();
        ReadingTypeInfoFactory readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        channelInfoFactory = new ChannelInfoFactory(clock, topologyService, readingTypeInfoFactory);
        when(readingTypeForChannel1.getMRID()).thenReturn(READING_TYPE_MRID_1);
        when(readingTypeForChannel2.getMRID()).thenReturn(READING_TYPE_MRID_2);
        when(readingTypeForChannel3.getMRID()).thenReturn(READING_TYPE_MRID_3);
        when(readingTypeForChannel4.getMRID()).thenReturn(READING_TYPE_MRID_4);
        when(readingTypeForChannel5.getMRID()).thenReturn(READING_TYPE_MRID_5);
        when(readingTypeForChannel6.getMRID()).thenReturn(READING_TYPE_MRID_6);

        when(readingTypeForChannel1.getAliasName()).thenReturn(READING_TYPE_MRID_1);
        when(readingTypeForChannel2.getAliasName()).thenReturn(READING_TYPE_MRID_2);
        when(readingTypeForChannel3.getAliasName()).thenReturn(READING_TYPE_MRID_3);
        when(readingTypeForChannel4.getAliasName()).thenReturn(READING_TYPE_MRID_4);
        when(readingTypeForChannel5.getAliasName()).thenReturn(READING_TYPE_MRID_5);
        when(readingTypeForChannel6.getAliasName()).thenReturn(READING_TYPE_MRID_6);

        prepareMockedReadingType(readingTypeForChannel1);
        prepareMockedReadingType(readingTypeForChannel2);
        prepareMockedReadingType(readingTypeForChannel3);
        prepareMockedReadingType(readingTypeForChannel4);
        prepareMockedReadingType(readingTypeForChannel5);
        prepareMockedReadingType(readingTypeForChannel6);

        IssueType dataValidationIssueType = mock(IssueType.class);
        IssueType dataCollectionIssueType = mock(IssueType.class);
        Finder<OpenIssue> issueFinder = mock(Finder.class);
        OpenIssue dataCollectionIssue1 = mockIssue(dataCollectionIssueType);
        OpenIssue dataCollectionIssue2 = mockIssue(dataCollectionIssueType);
        OpenIssue dataValidationIssue = mockIssue(dataValidationIssueType);
        when(dataValidationIssue.getId()).thenReturn(ISSUE_DATA_VALIDATION_ID);
        when(issueFinder.find()).thenReturn(Arrays.asList(dataCollectionIssue1, dataCollectionIssue2, dataValidationIssue));
        when(issueService.findOpenIssuesForDevice(DATALOGGER_NAME)).thenReturn(issueFinder);
        when(issueService.findStatus(IssueStatus.OPEN)).thenReturn(Optional.of(issueStatusOpen));
        when(issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME)).thenReturn(Optional.of(dataValidationIssueType));
        when(issueService.findIssueType(IssueDataCollectionService.DATA_COLLECTION_ISSUE)).thenReturn(Optional.of(dataCollectionIssueType));

        IssueDataValidation issueDataValidation = mock(IssueDataValidation.class);
        when(issueDataValidation.getId()).thenReturn(ISSUE_DATA_VALIDATION_ID);
        Finder<? extends IssueDataValidation> issueDataValidationFinder = mock(Finder.class);
        doReturn(Stream.of(new IssueDataValidation[]{issueDataValidation})).when(issueDataValidationFinder).stream();
        doReturn(issueDataValidationFinder).when(issueDataValidationService).findAllDataValidationIssues(any(DataValidationIssueFilter.class));
        when(meteringService.findAmrSystem(KnownAmrSystem.MDC.getId())).thenReturn(Optional.of(amrSystem));
        when(amrSystem.findMeter("" + DATALOGGER_ID)).thenReturn(Optional.of(meter));
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();

        when(topologyService.getSlaveChannel(any(Channel.class), any(Instant.class))).thenReturn(Optional.empty());

        when(topologyService.getPhysicalGateway(dataLogger)).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(eq(dataLoggerChn1), any(Instant.class))).thenReturn(Optional.of(slaveChn1));
        when(topologyService.getSlaveChannel(eq(dataLoggerChn2), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(eq(dataLoggerChn3), any(Instant.class))).thenReturn(Optional.of(slaveChn2));
        when(topologyService.getSlaveChannel(eq(dataLoggerChn4), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(eq(dataLoggerChn5), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.getSlaveChannel(eq(dataLoggerChn6), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(any(Channel.class))).thenReturn(Optional.empty());
        when(topologyService.availabilityDate(any(Register.class))).thenReturn(Optional.empty());

        when(channelSpec.getNbrOfFractionDigits()).thenReturn(2);
        when(channelSpec.isUseMultiplier()).thenReturn(false);
        when(channelSpec.getOverflow()).thenReturn(Optional.of(new BigDecimal(999999)));

        when(slave1.getDeviceType()).thenReturn(slaveDeviceType1);
        when(slaveDeviceType1.getName()).thenReturn(SLAVE_DEVICE_TYPE_NAME_1);
        when(slave1.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration1);
        when(slaveDeviceConfiguration1.getName()).thenReturn(SLAVE_DEVICE_CONFIGURATION_NAME_1);
        when(slave1.getId()).thenReturn(1L);
        when(slave1.getName()).thenReturn(SLAVE_1);
        when(slave1.getVersion()).thenReturn(1L);
        when(slave1.getBatch()).thenReturn(Optional.empty());
        CIMLifecycleDates lifecycleDatesSlave1 = mock(CIMLifecycleDates.class);
        when(lifecycleDatesSlave1.getReceivedDate()).thenReturn(Optional.of(LocalDateTime.of(2015, 8, 19, 0, 0).toInstant(ZoneOffset.UTC)));
        when(slave1.getLifecycleDates()).thenReturn(lifecycleDatesSlave1);

        when(slave2.getDeviceType()).thenReturn(slaveDeviceType2);
        when(slaveDeviceType2.getName()).thenReturn(SLAVE_DEVICE_TYPE_NAME_2);
        when(slave2.getDeviceConfiguration()).thenReturn(slaveDeviceConfiguration2);
        when(slaveDeviceConfiguration2.getName()).thenReturn(SLAVE_DEVICE_CONFIGURATION_NAME_2);
        when(slave2.getId()).thenReturn(2L);
        when(slave2.getName()).thenReturn(SLAVE_2);
        when(slave2.getVersion()).thenReturn(2L);
        when(slave2.getBatch()).thenReturn(Optional.empty());
        CIMLifecycleDates lifecycleDatesSlave2 = mock(CIMLifecycleDates.class);
        when(lifecycleDatesSlave2.getReceivedDate()).thenReturn(Optional.of(LocalDateTime.of(2015, 9, 1, 0, 0).toInstant(ZoneOffset.UTC)));
        when(slave2.getLifecycleDates()).thenReturn(lifecycleDatesSlave2);

        when(slaveChn1.getDevice()).thenReturn(slave1);
        when(slaveChn1.getReadingType()).thenReturn(readingTypeForChannel1);
        when(slaveChn1.getLoadProfile()).thenReturn(loadProfile3);
        when(slaveChn1.getId()).thenReturn(1000L);
        when(slaveChn1.getName()).thenReturn(SLAVE_CHANNEL_NAME_1);
        prepareMockedChannel(slaveChn1);

        when(slaveChn2.getDevice()).thenReturn(slave2);
        when(slaveChn2.getReadingType()).thenReturn(readingTypeForChannel3);
        when(slaveChn2.getLoadProfile()).thenReturn(loadProfile4);
        when(slaveChn2.getId()).thenReturn(2000L);
        when(slaveChn2.getName()).thenReturn(SLAVE_CHANNEL_NAME_2);
        prepareMockedChannel(slaveChn2);

        when(dataLogger.getBatch()).thenReturn(Optional.of(batch));
        when(batch.getName()).thenReturn(BATCH_NAME);

        when(deviceProtocolPluggableClass.getId()).thenReturn(PROTOCOL_ID);

        when(dataLoggerDeviceType.getId()).thenReturn(DATA_LOGGER_DEVICE_TYPE_ID);
        when(dataLoggerDeviceType.getName()).thenReturn(DATA_LOGGER_DEVICE_TYPE_NAME);
        when(dataLoggerDeviceType.getDeviceProtocolPluggableClass()).thenReturn(Optional.of(deviceProtocolPluggableClass));
        when(dataLoggerDeviceType.isDataloggerSlave()).thenReturn(false);

        when(dateLoggerDeviceConfiguration.getId()).thenReturn(DATALOGGER_DEVICE_CONFIGURATION_ID);
        when(dateLoggerDeviceConfiguration.getName()).thenReturn(DATALOGGER_DEVICE_CONFIGURATION_NAME);
        when(dateLoggerDeviceConfiguration.isDataloggerEnabled()).thenReturn(true);
        when(dateLoggerDeviceConfiguration.isDirectlyAddressable()).thenReturn(true);
        when(dateLoggerDeviceConfiguration.canActAsGateway()).thenReturn(false);
        when(dateLoggerDeviceConfiguration.getVersion()).thenReturn(DATALOGGER_DEVICE_CONFIGURATION_VERSION);

        when(usagePoint.getCurrentMeterActivations()).thenReturn(Collections.singletonList(meterActivation));
        when(usagePoint.getName()).thenReturn(USAGEPOINT_NAME);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);

        when(dataLogger.getUsagePoint()).thenReturn(Optional.of(usagePoint));
        when(serviceCategory.getName()).thenReturn(SERVICE_CATEGORY_NAME);

        when(deviceEstimation.isEstimationActive()).thenReturn(true);
        when(state.getName()).thenReturn(STATE_NAME);

        when(dataLoggerChn1.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChn2.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChn3.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChn4.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChn5.getDevice()).thenReturn(dataLogger);
        when(dataLoggerChn6.getDevice()).thenReturn(dataLogger);

        when(dataLoggerChn1.getReadingType()).thenReturn(readingTypeForChannel1);
        when(dataLoggerChn2.getReadingType()).thenReturn(readingTypeForChannel2);
        when(dataLoggerChn3.getReadingType()).thenReturn(readingTypeForChannel3);
        when(dataLoggerChn4.getReadingType()).thenReturn(readingTypeForChannel4);
        when(dataLoggerChn5.getReadingType()).thenReturn(readingTypeForChannel5);
        when(dataLoggerChn6.getReadingType()).thenReturn(readingTypeForChannel6);

        when(dataLoggerChn1.getObisCode()).thenReturn(new ObisCode(1, 0, 1, 8, 0, 255));
        when(dataLoggerChn2.getObisCode()).thenReturn(new ObisCode(1, 0, 2, 8, 0, 255));
        when(dataLoggerChn3.getObisCode()).thenReturn(new ObisCode(1, 0, 1, 8, 1, 255));
        when(dataLoggerChn4.getObisCode()).thenReturn(new ObisCode(1, 0, 2, 8, 2, 255));
        when(dataLoggerChn5.getObisCode()).thenReturn(new ObisCode(1, 0, 2, 8, 1, 255));
        when(dataLoggerChn6.getObisCode()).thenReturn(new ObisCode(1, 0, 2, 8, 2, 255));

        when(dataLoggerChn1.getLoadProfile()).thenReturn(loadProfile1);
        when(dataLoggerChn2.getLoadProfile()).thenReturn(loadProfile1);
        when(dataLoggerChn3.getLoadProfile()).thenReturn(loadProfile2);
        when(dataLoggerChn4.getLoadProfile()).thenReturn(loadProfile2);
        when(dataLoggerChn5.getLoadProfile()).thenReturn(loadProfile2);
        when(dataLoggerChn6.getLoadProfile()).thenReturn(loadProfile2);

        when(dataLoggerChn1.getId()).thenReturn(1L);
        when(dataLoggerChn2.getId()).thenReturn(2L);
        when(dataLoggerChn3.getId()).thenReturn(3L);
        when(dataLoggerChn4.getId()).thenReturn(4L);
        when(dataLoggerChn5.getId()).thenReturn(5L);
        when(dataLoggerChn6.getId()).thenReturn(6L);

        when(dataLoggerChn1.getName()).thenReturn("dataLoggerChn" + 1);
        when(dataLoggerChn2.getName()).thenReturn("dataLoggerChn" + 2);
        when(dataLoggerChn3.getName()).thenReturn("dataLoggerChn" + 3);
        when(dataLoggerChn4.getName()).thenReturn("dataLoggerChn" + 4);
        when(dataLoggerChn5.getName()).thenReturn("dataLoggerChn" + 5);
        when(dataLoggerChn6.getName()).thenReturn("dataLoggerChn" + 6);

        prepareMockedChannel(dataLoggerChn1);
        prepareMockedChannel(dataLoggerChn2);
        prepareMockedChannel(dataLoggerChn3);
        prepareMockedChannel(dataLoggerChn4);
        prepareMockedChannel(dataLoggerChn5);
        prepareMockedChannel(dataLoggerChn6);

        when(loadProfile1.getChannels()).thenReturn(Arrays.asList(dataLoggerChn1, dataLoggerChn2));
        when(loadProfile1.getId()).thenReturn(1L);
        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec1);
        when(loadProfileSpec1.getLoadProfileType()).thenReturn(loadProfileType1);
        when(loadProfileType1.getName()).thenReturn(LOADPROFILE_TYPE_NAME_1);
        when(loadProfile2.getChannels()).thenReturn(Arrays.asList(dataLoggerChn3, dataLoggerChn4, dataLoggerChn5, dataLoggerChn6));
        when(loadProfile2.getId()).thenReturn(2L);
        when(loadProfile2.getLoadProfileSpec()).thenReturn(loadProfileSpec2);
        when(loadProfileSpec2.getLoadProfileType()).thenReturn(loadProfileType2);
        when(loadProfileType2.getName()).thenReturn(LOADPROFILE_TYPE_NAME_2);
        when(loadProfile3.getChannels()).thenReturn(Collections.singletonList(slaveChn1));
        when(loadProfile3.getId()).thenReturn(3L);
        when(loadProfile3.getLoadProfileSpec()).thenReturn(loadProfileSpec3);
        when(loadProfileSpec3.getLoadProfileType()).thenReturn(loadProfileType3);
        when(loadProfileType3.getName()).thenReturn(LOADPROFILE_TYPE_NAME_3);
        when(loadProfile4.getChannels()).thenReturn(Collections.singletonList(slaveChn2));
        when(loadProfile4.getId()).thenReturn(4L);
        when(loadProfile4.getLoadProfileSpec()).thenReturn(loadProfileSpec4);
        when(loadProfileSpec4.getLoadProfileType()).thenReturn(loadProfileType4);
        when(loadProfileType4.getName()).thenReturn(LOADPROFILE_TYPE_NAME_4);

        when(dataLogger.getDeviceType()).thenReturn(dataLoggerDeviceType);
        when(dataLogger.getDeviceConfiguration()).thenReturn(dateLoggerDeviceConfiguration);
        when(dataLogger.getId()).thenReturn(DATALOGGER_ID);
        when(dataLogger.getName()).thenReturn(DATALOGGER_NAME);
        when(dataLogger.getSerialNumber()).thenReturn(DATALOGGER_SERIAL);
        when(dataLogger.getManufacturer()).thenReturn(DATALOGGER_MANUFACTURER);
        when(dataLogger.getModelNumber()).thenReturn(DATALOGGER_MODELNBR);
        when(dataLogger.getModelVersion()).thenReturn(DATALOGGER_MODELVERSION);
        when(dataLogger.getYearOfCertification()).thenReturn(DATALOGGER_YEAR_OF_CERTIFICATION);
        when(dataLogger.getConfigurationGatewayType()).thenReturn(GatewayType.NONE);
        when(dataLogger.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(dataLogger.getLogBooks()).thenReturn(Arrays.asList(logBook1, logBook2, logBook3));
        when(dataLogger.getRegisters()).thenReturn(Collections.emptyList());
        doReturn(Optional.of(meterActivation)).when(dataLogger).getCurrentMeterActivation();
        when(dataLogger.getState()).thenReturn(state);
        when(dataLogger.getVersion()).thenReturn(DATALOGGER_VERSION);
        when(dataLogger.forEstimation()).thenReturn(deviceEstimation);
        when(dataLogger.getChannels()).thenReturn(Arrays.asList(dataLoggerChn1, dataLoggerChn2, dataLoggerChn3, dataLoggerChn4, dataLoggerChn5, dataLoggerChn6));
        when(dataLogger.getLocation()).thenReturn(Optional.empty());
        when(dataLogger.getSpatialCoordinates()).thenReturn(Optional.empty());

        CIMLifecycleDates lifecycleDates = mock(CIMLifecycleDates.class);
        when(lifecycleDates.getReceivedDate()).thenReturn(Optional.of(LocalDateTime.of(2015, 7, 13, 12, 0).toInstant(ZoneOffset.UTC)));
        when(dataLogger.getLifecycleDates()).thenReturn(lifecycleDates);
        when(topologyService.findDataloggerReference(any(Device.class), any(Instant.class))).thenReturn(Optional.empty());
        when(topologyService.findLastDataloggerReference(any(Device.class))).thenReturn(Optional.empty());
    }

    private void setupTranslations() {
        when(thesaurus.getString(any(), any())).thenReturn("Translation not supported in unit tests");
        when(this.deviceLifeCycleConfigurationService.getDisplayName(any(DefaultState.class)))
                .thenAnswer(invocationOnMock -> {
                    DefaultState state = (DefaultState) invocationOnMock.getArguments()[0];
                    return state.getDefaultFormat();
                });
    }

    private OpenIssue mockIssue(IssueType dataCollectionIssueType) {
        OpenIssue issue = mock(OpenIssue.class);
        IssueReason issueReason = mock(IssueReason.class);
        when(issueReason.getIssueType()).thenReturn(dataCollectionIssueType);
        when(issue.getReason()).thenReturn(issueReason);
        return issue;
    }

    private void prepareMockedReadingType(ReadingType readingType) {
        when(readingType.getName()).thenReturn("readingTypeName");
        when(readingType.isActive()).thenReturn(true);
        when(readingType.isCumulative()).thenReturn(true);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.NORMAL);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.MINUTE15);
        when(readingType.getAccumulation()).thenReturn(Accumulation.CUMULATIVE);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.NOTAPPLICABLE);
        when(readingType.getCommodity()).thenReturn(Commodity.DEVICE);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.CURRENT);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(0, 1));
        when(readingType.getArgument()).thenReturn(new RationalNumber(0, 1));
        when(readingType.getTou()).thenReturn(0);
        when(readingType.getCpp()).thenReturn(0);
        when(readingType.getConsumptionTier()).thenReturn(0);
        when(readingType.getPhases()).thenReturn(Phase.PHASES1);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.KILO);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.WATTHOUR);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(readingType.getVersion()).thenReturn(1L);
    }

    private void prepareMockedChannel(Channel mockedChannel) {
        when(mockedChannel.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(mockedChannel.getLastReading()).thenReturn(Optional.empty());
        when(mockedChannel.getLastDateTime()).thenReturn(Optional.empty());
        when(mockedChannel.getCalculatedReadingType(any(Instant.class))).thenReturn(Optional.empty());
        when(mockedChannel.getOverflow()).thenReturn(Optional.empty());
        when(mockedChannel.getUnit()).thenReturn(kiloWattHours);
        when(mockedChannel.getChannelSpec()).thenReturn(channelSpec);
        when(mockedChannel.getMultiplier(any(Instant.class))).thenReturn(Optional.empty());
        when(mockedChannel.getOverflow()).thenReturn(Optional.empty());
    }

    @Test
    public void fromDataLoggerTest() {
        DataLoggerSlaveDeviceInfoFactory dataLoggerSlaveDeviceInfoFactory = new DataLoggerSlaveDeviceInfoFactory(Clock.systemUTC(), topologyService, deviceDataInfoFactory, batchService, channelInfoFactory);

        DeviceInfoFactory deviceInfoFactory = new DeviceInfoFactory(thesaurus, batchService, topologyService, issueService, dataLoggerSlaveDeviceInfoFactory, deviceService, deviceLifeCycleConfigurationService, clock);
        DeviceInfo info = deviceInfoFactory.deviceInfo(dataLogger);

        assertThat(info.id).isEqualTo(DATALOGGER_ID);
        assertThat(info.name).isEqualTo(DATALOGGER_NAME);
        assertThat(info.serialNumber).isEqualTo(DATALOGGER_SERIAL);
        assertThat(info.manufacturer).isEqualTo(DATALOGGER_MANUFACTURER);
        assertThat(info.modelNbr).isEqualTo(DATALOGGER_MODELNBR);
        assertThat(info.modelVersion).isEqualTo(DATALOGGER_MODELVERSION);
        assertThat(info.deviceTypeId).isEqualTo(DATA_LOGGER_DEVICE_TYPE_ID);
        assertThat(info.deviceTypeName).isEqualTo(DATA_LOGGER_DEVICE_TYPE_NAME);
        assertThat(info.deviceConfigurationId).isEqualTo(DATALOGGER_DEVICE_CONFIGURATION_ID);
        assertThat(info.deviceConfigurationName).isEqualTo(DATALOGGER_DEVICE_CONFIGURATION_NAME);
        assertThat(info.deviceProtocolPluggeableClassId).isEqualTo(PROTOCOL_ID);
        assertThat(info.yearOfCertification).isEqualTo(DATALOGGER_YEAR_OF_CERTIFICATION);
        assertThat(info.batch).isEqualTo(BATCH_NAME);
        assertThat(info.masterDeviceId).isNull();
        assertThat(info.masterDeviceName).isNull();
        assertThat(info.gatewayType).isEqualTo(GatewayType.NONE);
        assertThat(info.slaveDevices).isEmpty();
        assertThat(info.nbrOfDataCollectionIssues).isEqualTo(ISSUE_COUNT);
        assertThat(info.openDataValidationIssue).isEqualTo(ISSUE_DATA_VALIDATION_ID);
        assertThat(info.hasLoadProfiles).isTrue();
        assertThat(info.hasLogBooks).isTrue();
        assertThat(info.hasRegisters).isFalse();
        assertThat(info.isDirectlyAddressed).isTrue();
        assertThat(info.isGateway).isFalse();
        assertThat(info.isDataLogger).isTrue();
        assertThat(info.isDataLoggerSlave).isFalse();
        assertThat(info.usagePoint).isEqualTo(USAGEPOINT_NAME);
        assertThat(info.serviceCategory).isEqualTo(SERVICE_CATEGORY_NAME);
        assertThat(info.estimationStatus.active).isTrue();
        assertThat(info.state.name).isEqualTo(STATE_TRANSLATION);
        assertThat(info.version).isEqualTo(DATALOGGER_VERSION);
        assertThat(info.parent.id).isEqualTo(DATALOGGER_DEVICE_CONFIGURATION_ID);
        assertThat(info.parent.version).isEqualTo(DATALOGGER_DEVICE_CONFIGURATION_VERSION);
        // Data logger stuff
        assertThat(info.dataLoggerSlaveDevices).hasSize(3);  // 2 slaves + 1 for unlinked channels

        assertThat(info.dataLoggerSlaveDevices.get(0).name).isEqualTo(SLAVE_1);
        assertThat(info.dataLoggerSlaveDevices.get(0).dataLoggerSlaveChannelInfos).hasSize(1);
        assertThat(info.dataLoggerSlaveDevices.get(0).dataLoggerSlaveChannelInfos.get(0).slaveChannel.name).isEqualTo(SLAVE_CHANNEL_NAME_1);
        assertThat(info.dataLoggerSlaveDevices.get(0).dataLoggerSlaveChannelInfos.get(0).dataLoggerChannel.name).isEqualTo("dataLoggerChn1");

        assertThat(info.dataLoggerSlaveDevices.get(1).placeHolderForUnlinkedDataLoggerChannelsAndRegisters()).isTrue();
        assertThat(info.dataLoggerSlaveDevices.get(1).dataLoggerSlaveChannelInfos).hasSize(4);

        assertThat(info.dataLoggerSlaveDevices.get(2).name).isEqualTo(SLAVE_2);
        assertThat(info.dataLoggerSlaveDevices.get(2).dataLoggerSlaveChannelInfos).hasSize(1);
        assertThat(info.dataLoggerSlaveDevices.get(2).dataLoggerSlaveChannelInfos.get(0).slaveChannel.name).isEqualTo(SLAVE_CHANNEL_NAME_2);
        assertThat(info.dataLoggerSlaveDevices.get(2).dataLoggerSlaveChannelInfos.get(0).dataLoggerChannel.name).isEqualTo("dataLoggerChn3");
    }
}
