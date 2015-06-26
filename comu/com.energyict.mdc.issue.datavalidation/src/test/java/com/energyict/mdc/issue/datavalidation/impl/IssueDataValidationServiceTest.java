package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueProvider;
import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ListValue;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.issue.datavalidation.*;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueDataValidationServiceTest extends PersistenceIntegrationTest {

    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private IssueDataValidationService issueDataValidationService;
    private CreationRule issueCreationRule;

    @Before
    public void setUp() throws Exception {
        issueService = inMemoryPersistence.getService(IssueService.class);
        DataValidationIssueCreationRuleTemplate template = inMemoryPersistence.getService(DataValidationIssueCreationRuleTemplate.class);
        ((IssueServiceImpl)issueService).addCreationRuleTemplate(template);
        issueCreationService = issueService.getIssueCreationService();
        issueDataValidationService = inMemoryPersistence.getService(IssueDataValidationService.class);
        
        CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        ListValue<HasIdAndName> value = new ListValue<>();
        HasIdAndName deviceConfig = mock(HasIdAndName.class);
        when(deviceConfig.getId()).thenReturn(1L);
        value.addValue(deviceConfig);
        props.put(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, value);
        issueCreationRule = ruleBuilder.setTemplate(DataValidationIssueCreationRuleTemplate.NAME)
                   .setName("Test")
                   .setIssueType(issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get())
                   .setReason(issueService.findReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get())
                   .setDueInTime(DueInType.YEAR, 5)
                   .setProperties(props)
                   .complete();
        issueCreationRule.save();
    }
    
    @Test
    @Transactional
    public void testFindIssueById() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(2);
        
        long firstIssue = issues.get(0).getId();
        long secondIssue = issues.get(1).getId();
        
        Optional<? extends IssueDataValidation> issue = issueDataValidationService.findIssue(firstIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof OpenIssueDataValidation).isTrue();

        //close first issue
        OpenIssueDataValidation closedIssue = (OpenIssueDataValidation)issue.get(); 
        closedIssue.close(issueService.findStatus(IssueStatus.RESOLVED).get());
        
        issue = issueDataValidationService.findIssue(firstIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof HistoricalIssueDataValidation).isTrue();
        
        issue = issueDataValidationService.findIssue(secondIssue);
        assertThat(issue).isPresent();
        assertThat(issue.get() instanceof OpenIssueDataValidation).isTrue();        
        
        issue = issueDataValidationService.findOpenIssue(firstIssue);
        assertThat(issue).isEmpty();
        
        issue = issueDataValidationService.findHistoricalIssue(firstIssue);
        assertThat(issue).isPresent();
        
        issue = issueDataValidationService.findOpenIssue(secondIssue);
        assertThat(issue).isPresent();
    }
    
    @Test
    @Transactional
    public void testFindAllDataValidationIssuesFilterByAssignee() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);
        
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.setUnassignedOnly();
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        
        User assignee = inMemoryPersistence.getService(UserService.class).findOrCreateUser("User", "Local", "Type");
        assignee.save();
        IssueDataValidation issue = issueDataValidationService.findOpenIssue(baseIssues.get(0).getId()).get();
        issue.assignTo(IssueAssignee.Types.USER, assignee.getId());
        issue.save();
        
        filter = new DataValidationIssueFilter();
        filter.setUnassignedOnly();
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
        
        filter = new DataValidationIssueFilter();
        filter.setAssignee(assignee);
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getAssignee().getId()).isEqualTo(assignee.getId());

        filter = new DataValidationIssueFilter();
        User anotherAssignee = inMemoryPersistence.getService(UserService.class).findOrCreateUser("AnotherUser", "Local", "Type");
        anotherAssignee.save();
        filter.setAssignee(anotherAssignee);
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }
    
    @Test
    @Transactional
    public void testFindAllDataValidationIssuesFilterByReason() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);
        
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.setIssueReason(issueService.findReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get());
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        
        filter = new DataValidationIssueFilter();
        IssueReason reason = issueService.createReason("somereason", issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get(), MessageSeeds.DATA_VALIDATION_ISSUE_REASON);
        reason.save();
        filter.setIssueReason(reason);
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }
    
    @Test
    @Transactional
    public void testFindAllDataValidationIssuesFilterByDevice() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);
        
        AmrSystem amrSystem = inMemoryPersistence.getService(MeteringService.class).findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        EndDevice endDevice = amrSystem.newEndDevice("METER");
        endDevice.save();
        IssueDataValidation issue = issueDataValidationService.findOpenIssue(baseIssues.get(0).getId()).get();
        issue.setDevice(endDevice);
        issue.save();
        
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.setDevice(endDevice);
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(issue.getDevice()).isEqualTo(endDevice);
        
        endDevice = amrSystem.newEndDevice("ANOTHER METER");
        endDevice.save();
        filter = new DataValidationIssueFilter();
        filter.setDevice(endDevice);
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }

    @Test
    @Transactional
    public void testFindAllDataValidationIssuesFilterByStatus() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);
        
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssues.get(0).getId());
        assertThat(issue.getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        
        filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        
        filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.IN_PROGRESS).get());
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
        
        filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        filter.addStatus(issueService.findStatus(IssueStatus.WONT_FIX).get());
        issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).isEmpty();
    }
    
    @Test
    @Transactional
    public void testCreateIssueWithNonEstimatedBlocks() {
        MeteringService meteringService = inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem  = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter");
        ReadingType readingType1Min = meteringService.createReadingType("0.0.3.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 1-minute");
        ReadingType readingType3Min = meteringService.createReadingType("0.0.14.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 3-minute");
        MeterActivation meterActivation = newMeter.activate(Instant.now());
        Channel channelRT1 = meterActivation.createChannel(readingType1Min);
        Channel channelRT2 = meterActivation.createChannel(readingType3Min);
        
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        OpenIssueDataValidation dataValidationIssue = issueDataValidationService.findOpenIssue(issues.get(0).getId()).get();
        
        Instant now = Instant.now();
        
        dataValidationIssue.addNotEstimatedBlock(channelRT1, readingType1Min, now, now);
        dataValidationIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now, now);
        dataValidationIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now.plus(3, ChronoUnit.MINUTES), now.plus(3, ChronoUnit.MINUTES));
        dataValidationIssue.save();
        
        dataValidationIssue = issueDataValidationService.findOpenIssue(dataValidationIssue.getId()).get();
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(2);
        NotEstimatedBlock block1 = dataValidationIssue.getNotEstimatedBlocks().get(0);
        assertThat(block1.getChannel()).isEqualTo(channelRT1);
        assertThat(block1.getReadingType()).isEqualTo(readingType1Min);
        assertThat(block1.getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(block1.getEndTime()).isEqualTo(now);
        
        NotEstimatedBlock block2 = dataValidationIssue.getNotEstimatedBlocks().get(1);
        assertThat(block2.getChannel()).isEqualTo(channelRT2);
        assertThat(block2.getReadingType()).isEqualTo(readingType3Min);
        assertThat(block2.getStartTime()).isEqualTo(now.minus(3, ChronoUnit.MINUTES));
        assertThat(block2.getEndTime()).isEqualTo(now.plus(3, ChronoUnit.MINUTES));
    }
    
    @Test
    @Transactional
    public void testUpdateIssueWithNonEstimatedBlocks() {
        MeteringService meteringService = inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem  = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter");
        ReadingType readingType = meteringService.createReadingType("0.0.3.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 1-minute");
        Channel channel = newMeter.activate(Instant.now()).createChannel(readingType);
        
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        OpenIssueDataValidation dataValidationIssue = issueDataValidationService.findOpenIssue(issues.get(0).getId()).get();
        
        Instant now = Instant.now();
        
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES), now.minus(1, ChronoUnit.MINUTES));//(now-2min, now-1min]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.plus(1, ChronoUnit.MINUTES), now.plus(1, ChronoUnit.MINUTES));//(now, now+1min]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.plus(3, ChronoUnit.MINUTES), now.plus(3, ChronoUnit.MINUTES));//(now+2min, now+3min]
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now, now.plus(4, ChronoUnit.MINUTES));//(now-1min, now + 4min]
        dataValidationIssue.save();//(now-2min, now+4min]
        
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(2, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(4, ChronoUnit.MINUTES));
        
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now);//(now-1min, now]
        dataValidationIssue.save();//(now-2min, now-1min] + (now, now+4min]
        
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(2);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(2, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getStartTime()).isEqualTo(now);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getEndTime()).isEqualTo(now.plus(4, ChronoUnit.MINUTES));
        
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now, now);//(now-1min, now]
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.minus(300, ChronoUnit.MINUTES));//(now-299min, now-300min]
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES));//(now-2min, now-1min]
        dataValidationIssue.save();//(now-1min, now+4min]
        
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(4, ChronoUnit.MINUTES));
        
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES), now);//(now-2min, now]
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.plus(300, ChronoUnit.MINUTES));//(now+299min, now+300min]
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.plus(1, ChronoUnit.MINUTES));//(now, now+1min]
        dataValidationIssue.save();//(now-2min, now] (now+1min, now+4min]
        
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(2);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(2, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getStartTime()).isEqualTo(now.plus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getEndTime()).isEqualTo(now.plus(4, ChronoUnit.MINUTES));
    }
    
    @Test
    @Transactional
    public void testIssueProvider() {
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        IssueProvider issueProvider = (IssueDataValidationServiceImpl) issueDataValidationService;
        
        List<OpenIssue> baseIssues = issueService.query(OpenIssue.class).select(Condition.TRUE);
        assertThat(baseIssues).hasSize(1);
        
        OpenIssue openBaseIssue = baseIssues.get(0);
        
        Optional<? extends OpenIssue> openIssueDV = issueProvider.getOpenIssue(openBaseIssue);
        assertThat(openIssueDV).isPresent();
        assertThat(openIssueDV.get() instanceof OpenIssueDataValidation).isTrue();
        assertThat(openIssueDV.get().getId()).isEqualTo(openBaseIssue.getId());
        
        assertThat(issueProvider.getOpenIssue(openIssueDV.get()).get()).isEqualTo(openIssueDV.get());
        
        openIssueDV.get().close(issueService.findStatus(IssueStatus.WONT_FIX).get());
        
        HistoricalIssue historicalBaseIssue = issueService.query(HistoricalIssue.class).select(Condition.TRUE).get(0);
        
        Optional<? extends HistoricalIssue> historicalIssueDV = issueProvider.getHistoricalIssue(historicalBaseIssue);
        assertThat(historicalIssueDV).isPresent();
        assertThat(historicalIssueDV.get() instanceof HistoricalIssueDataValidation).isTrue();
        assertThat(historicalIssueDV.get().getId()).isEqualTo(historicalBaseIssue.getId());
        
        assertThat(issueProvider.getHistoricalIssue(historicalIssueDV.get()).get()).isEqualTo(historicalIssueDV.get());
    }
    
    @Test
    @Transactional
    public void testCloseIssue() {
        MeteringService meteringService = inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem  = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter");
        ReadingType readingType1Min = meteringService.createReadingType("0.0.3.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 1-minute");
        ReadingType readingType3Min = meteringService.createReadingType("0.0.14.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "Fake RT with timeperiod 3-minute");
        MeterActivation meterActivation = newMeter.activate(Instant.now());
        Channel channelRT1 = meterActivation.createChannel(readingType1Min);
        Channel channelRT2 = meterActivation.createChannel(readingType3Min);
        
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);
        
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        List<? extends IssueDataValidation> openIssues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(openIssues).hasSize(1);
        OpenIssueDataValidation openIssue = (OpenIssueDataValidation) openIssues.get(0);
        
        Instant now = Instant.now();
        openIssue.addNotEstimatedBlock(channelRT1, readingType1Min, now, now);//(now-1min, now]
        openIssue.addNotEstimatedBlock(channelRT2, readingType3Min, now.plus(300, ChronoUnit.MINUTES), now.plus(300, ChronoUnit.MINUTES));//(now+297min, now+300min]
        openIssue.save();
        openIssue.close(issueService.findStatus(IssueStatus.WONT_FIX).get());
        
        filter = new DataValidationIssueFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        assertThat(issueDataValidationService.findAllDataValidationIssues(filter).find()).isEmpty();
        assertThat(issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find()).hasSize(1);
        
        Optional<HistoricalIssueDataValidation> historicalIssue = issueDataValidationService.findHistoricalIssue(openIssue.getId());
        assertThat(historicalIssue).isPresent();
        assertThat(historicalIssue.get().getId()).isEqualTo(openIssue.getId());
        assertThat(historicalIssue.get().getRule().getId()).isEqualTo(issueCreationRule.getId());
        assertThat(historicalIssue.get().getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(historicalIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
        List<NotEstimatedBlock> notEstimatedBlocks = historicalIssue.get().getNotEstimatedBlocks();
        assertThat(notEstimatedBlocks).hasSize(2);
        
        NotEstimatedBlock block1 = notEstimatedBlocks.get(0);
        assertThat(block1.getChannel()).isEqualTo(channelRT1);
        assertThat(block1.getReadingType()).isEqualTo(readingType1Min);
        assertThat(block1.getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(block1.getEndTime()).isEqualTo(now);
        
        NotEstimatedBlock block2 = notEstimatedBlocks.get(1);
        assertThat(block2.getChannel()).isEqualTo(channelRT2);
        assertThat(block2.getReadingType()).isEqualTo(readingType3Min);
        assertThat(block2.getStartTime()).isEqualTo(now.plus(297, ChronoUnit.MINUTES));
        assertThat(block2.getEndTime()).isEqualTo(now.plus(300, ChronoUnit.MINUTES));
    }

    @Test
    @Transactional
    public void testCloseBaseIssue() {
        ((IssueServiceImpl)issueService).addIssueProvider((IssueProvider) issueDataValidationService);

        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(issueCreationRule.getId(), event);

        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(new DataValidationIssueFilter()).find();
        assertThat(issues).hasSize(1);
        Optional<? extends Issue> baseIssue = issueService.findIssue(issues.get(0).getId());
        assertThat(baseIssue.get() instanceof OpenIssueImpl).isTrue();
        ((OpenIssue)baseIssue.get()).close(issueService.findStatus(IssueStatus.WONT_FIX).get());
        baseIssue = issueService.findIssue(issues.get(0).getId());
        assertThat(baseIssue.get() instanceof HistoricalIssueImpl).isTrue();
        assertThat(baseIssue.get().getStatus().getKey()).isEqualTo(IssueStatus.WONT_FIX);
        assertThat(issueDataValidationService.findHistoricalIssue(baseIssue.get().getId())).isPresent();
    }
}
