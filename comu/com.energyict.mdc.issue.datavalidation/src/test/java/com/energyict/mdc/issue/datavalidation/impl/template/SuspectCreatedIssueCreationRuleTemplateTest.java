package com.energyict.mdc.issue.datavalidation.impl.template;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.device.data.impl.DeviceDataModelService;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.event.DataValidationEventHandlerFactory;
import org.junit.*;

import java.sql.SQLException;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Ignore
public class SuspectCreatedIssueCreationRuleTemplateTest extends BaseTemplateTest {

    private static final String TEST_NAME = SuspectCreatedIssueCreationRuleTemplateTest.class.getName();

    // Change this variable if you want to see sql logs
    private static final boolean SHOW_SQL_LOGS = false;

    private SuspectCreatedIssueCreationRuleTemplate issueCreationRuleTemplate;
    private DeviceType deviceType;
    private DeviceConfiguration deviceConfiguration;
    private Meter meter;
    private Channel channel;
    private ReadingType readingType;

    @BeforeClass
    public static void setUpClass() throws SQLException {
        initializeClock();
        inMemoryPersistence.initializeDatabase(TEST_NAME, SHOW_SQL_LOGS);
        when(inMemoryPersistence.getClock().instant()).thenReturn(TIME);

        try (TransactionContext transactionContext = inMemoryPersistence.getTransactionService().getContext()) {
            inMemoryPersistence.getService(FiniteStateMachineService.class);
            inMemoryPersistence.getService(IssueDataValidationService.class);
            inMemoryPersistence.getService(DeviceDataModelService.class);
            inMemoryPersistence.getService(MeteringService.class);
            transactionContext.commit();
        }
    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Before
    public void setUp() throws Exception {
        // Setting up services
        issueCreationRuleTemplate = inMemoryPersistence.getService(SuspectCreatedIssueCreationRuleTemplate.class);
        issueService = inMemoryPersistence.getService(IssueService.class);
        issueCreationService = issueService.getIssueCreationService();
        issueDataValidationService = inMemoryPersistence.getService(IssueDataValidationService.class);
        messageHandler = inMemoryPersistence.getService(DataValidationEventHandlerFactory.class).newMessageHandler();
        meteringService = inMemoryPersistence.getService(MeteringService.class);

        // Adding template to issue service
        issueService.addCreationRuleTemplate(issueCreationRuleTemplate);

        // Creating device types, configuration, reading types and etc.
        deviceType = createDeviceType();
        deviceConfiguration = createDeviceConfiguration(deviceType, "Default");
        readingType = meteringService.createReadingType(createReadingTypeCode(), "RT");
        meter = createMeter(deviceConfiguration, "Test Meter", TIME);
        channel = meter.getCurrentMeterActivation().get().getChannelsContainer().createChannel(readingType);

        createRuleForDeviceConfiguration("Default Rule", deviceType, deviceConfiguration);

        // Verifing that drools rules are compiled without errors
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
    }

    @After
    public void tearDown() throws Exception {
        // NoOp
    }

    @Test
    @Transactional
    public void shouldReturnIssueTypeDataValidation() {
        final IssueType actualIssueType = issueCreationRuleTemplate.getIssueType();
        final IssueType expectedIssueType = issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get();

        assertThat(actualIssueType.getId()).isEqualTo(expectedIssueType.getId());
    }

    private static void initializeClock() {
        when(inMemoryPersistence.getClock().getZone()).thenReturn(TIME_ZONE.toZoneId());
        when(inMemoryPersistence.getClock().instant()).thenAnswer(invocationOnMock -> Instant.now());
    }
}