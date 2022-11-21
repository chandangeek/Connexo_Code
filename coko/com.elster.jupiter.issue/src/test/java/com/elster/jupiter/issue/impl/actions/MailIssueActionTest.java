package com.elster.jupiter.issue.impl.actions;
import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.fsm.StateTimeline;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceEventRecordFilterSpecification;
import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventRecordBuilder;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import com.google.common.collect.Range;
import org.osgi.framework.BundleContext;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MailIssueActionTest extends BaseTest{
    IssueAction action;
    BundleContext bundleContext;
    Issue issue;

    private static final String MAIL_SMTP_HOST_PROPERTY = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT_PROPERTY = "mail.smtp.port";
    private static final String MAIL_USER_PROPERTY = "mail.user";
    private static final String MAIL_PASSWORD_PROPERTY = "mail.password";
    private static final String MAIL_FROM_PROPERTY = "mail.from";

    @Before
    public void setUp() throws Exception {
        action = getDefaultActionsFactory().createIssueAction(MailIssueAction.class.getName());
        bundleContext = ((IssueServiceImpl)issueService).getBundleContext().get();
        issue = createIssueMinInfo();
    }

    @Test
    @Transactional
    public void testCreateAndExecuteAction() {
        when(bundleContext.getProperty(MAIL_SMTP_HOST_PROPERTY)).thenReturn("smtp.honeywell.com");
        when(bundleContext.getProperty(MAIL_SMTP_PORT_PROPERTY)).thenReturn("25");
        when(bundleContext.getProperty(MAIL_USER_PROPERTY)).thenReturn("venkatakrishna.alisetty@honeywell.com");
        when(bundleContext.getProperty(MAIL_PASSWORD_PROPERTY)).thenReturn("Honeywell@123");
        when(bundleContext.getProperty(MAIL_FROM_PROPERTY)).thenReturn("venkatakrishna.alisetty@honeywell.com");
        Map<String, Object> properties = new HashMap<>();
        properties.put(MailIssueAction.TO, new MailIssueAction.MailTo("venkatakrishnaalisetty@honeywell.com"));
        Issue issue = createIssueMinInfo();
        issue.setDevice(new EndDevice() {
            @Override
            public String getSerialNumber() {
                return null;
            }

            @Override
            public String getUtcNumber() {
                return null;
            }

            @Override
            public ElectronicAddress getElectronicAddress() {
                return null;
            }

            @Override
            public AmrSystem getAmrSystem() {
                return null;
            }

            @Override
            public String getAmrId() {
                return null;
            }

            @Override
            public void update() {

            }

            @Override
            public Instant getCreateTime() {
                return null;
            }

            @Override
            public Instant getModTime() {
                return null;
            }

            @Override
            public long getVersion() {
                return 0;
            }

            @Override
            public void delete() {

            }

            @Override
            public Optional<HeadEndInterface> getHeadEndInterface() {
                return Optional.empty();
            }

            @Override
            public EndDeviceEventRecordBuilder addEventRecord(EndDeviceEventType type, Instant instant, long logBookId) {
                return null;
            }

            @Override
            public List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range) {
                return null;
            }

            @Override
            public List<EndDeviceEventRecord> getDeviceEvents(Range<Instant> range, List<EndDeviceEventType> eventTypes) {
                return null;
            }

            @Override
            public long getDeviceEventsCountByFilter(EndDeviceEventRecordFilterSpecification filter) {
                return 0;
            }

            @Override
            public List<EndDeviceEventRecord> getDeviceEventsByFilter(EndDeviceEventRecordFilterSpecification filter) {
                return null;
            }

            @Override
            public List<EndDeviceEventRecord> getDeviceEventsByReadTime(Range<Instant> range) {
                return null;
            }

            @Override
            public void setSerialNumber(String serialNumber) {

            }

            @Override
            public void setLocation(Location location) {

            }

            @Override
            public Optional<Location> getLocation() {
                return Optional.empty();
            }

            @Override
            public Optional<SpatialCoordinates> getSpatialCoordinates() {
                return Optional.empty();
            }

            @Override
            public void setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {

            }

            @Override
            public Optional<FiniteStateMachine> getFiniteStateMachine() {
                return Optional.empty();
            }

            @Override
            public Optional<State> getState() {
                return Optional.empty();
            }

            @Override
            public Optional<State> getState(Instant instant) {
                return Optional.empty();
            }

            @Override
            public Optional<StateTimeline> getStateTimeline() {
                return Optional.empty();
            }

            @Override
            public LifecycleDates getLifecycleDates() {
                return null;
            }

            @Override
            public void setName(String name) {

            }

            @Override
            public boolean isObsolete() {
                return false;
            }

            @Override
            public void makeObsolete() {

            }

            @Override
            public Optional<Instant> getObsoleteTime() {
                return Optional.empty();
            }

            @Override
            public String getManufacturer() {
                return null;
            }

            @Override
            public void setManufacturer(String manufacturer) {

            }

            @Override
            public String getModelNumber() {
                return null;
            }

            @Override
            public void setModelNumber(String modelNumber) {

            }

            @Override
            public String getModelVersion() {
                return null;
            }

            @Override
            public void setModelVersion(String modelVersion) {

            }

            @Override
            public String getAliasName() {
                return null;
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public String getMRID() {
                return null;
            }

            @Override
            public long getId() {
                return 0;
            }

            @Override
            public String getName() {
                return "Test";
            }
        });
        issue.getPriority();
        IssueAction actionResult = action.initAndValidate(properties);

        assertThat(issue.getDevice().getName()).isEqualTo("Test");
        assertThat(issue.getAssignee().getUser()).isNull();
        assertThat(actionResult.getDisplayName()).isEqualTo("Email");
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.PROPERTY_MISSING +"}", property = "properties.MailIssueAction.to", strict = true)
    public void testValidateMandatoryParameters() {
        action.initAndValidate(new HashMap<>());
    }

}
