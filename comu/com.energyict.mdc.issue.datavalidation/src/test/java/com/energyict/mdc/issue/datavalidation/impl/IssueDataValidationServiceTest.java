package com.energyict.mdc.issue.datavalidation.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.service.IssueServiceImpl;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.DueInType;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueCreationService;
import com.elster.jupiter.issue.share.service.IssueCreationService.CreationRuleBuilder;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;

public class IssueDataValidationServiceTest extends PersistenceIntegrationTest {

    private IssueService issueService;
    private IssueCreationService issueCreationService;
    private IssueDataValidationService issueDataValidationService;

    @Before
    public void setUp() throws Exception {
        issueService = inMemoryPersistence.getService(IssueService.class);
        DataValidationIssueCreationRuleTemplate template = inMemoryPersistence.getService(DataValidationIssueCreationRuleTemplate.class);
        ((IssueServiceImpl)issueService).addCreationRuleTemplate(template);
        issueCreationService = issueService.getIssueCreationService();
        issueDataValidationService = inMemoryPersistence.getService(IssueDataValidationService.class);
    }
    
    @Test
    @Transactional
    public void testNominalCase() {
        CreationRuleBuilder ruleBuilder = issueCreationService.newCreationRule();
        Map<String, Object> props = new HashMap<>();
        props.put(DataValidationIssueCreationRuleTemplate.DEVICE_CONFIGURATIONS, "1,2,3");
        CreationRule rule = ruleBuilder.setTemplate(DataValidationIssueCreationRuleTemplate.NAME)
                   .setName("Test")
                   .setReason(issueService.findReason(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON).get())
                   .setDueInTime(DueInType.YEAR, 5)
                   .setProperties(props)
                   .complete();
        rule.save();
        
        IssueEvent event = mock(IssueEvent.class);
        when(event.findExistingIssue()).thenReturn(Optional.empty());
        issueCreationService.processIssueCreationEvent(rule.getId(), event);
        
        assertThat(issueService.query(OpenIssue.class).select(Condition.TRUE)).hasSize(1);
        long baseIssueId = issueService.query(OpenIssue.class).select(Condition.TRUE).get(0).getId();
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
//        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
//        filter.addStatus(issueService.findStatus(IssueStatus.RESOLVED).get());
        List<? extends IssueDataValidation> issues = issueDataValidationService.findAllDataValidationIssues(filter).find();
        assertThat(issues).hasSize(1);
        IssueDataValidation issue = issues.get(0);
        assertThat(issue.getId()).isEqualTo(baseIssueId);
        assertThat(issue.getRule().getId()).isEqualTo(rule.getId());
        assertThat(issue.getReason().getKey()).isEqualTo(IssueDataValidationService.DATA_VALIDATION_ISSUE_REASON);
        assertThat(issue.getStatus().getKey()).isEqualTo(IssueStatus.OPEN);
        
        assertThat(issueDataValidationService.findOpenIssue(baseIssueId).isPresent()).isTrue();
        
        MeteringService meteringService = inMemoryPersistence.getService(MeteringService.class);
        AmrSystem amrSystem  = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId()).get();
        Meter newMeter = amrSystem.newMeter("Meter");
        ReadingType readingType = meteringService.createReadingType("0.0.3.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0", "NULL");
        Channel channel = newMeter.activate(Instant.now()).createChannel(readingType);
        
        OpenIssueDataValidation dataValidationIssue = ((OpenIssueDataValidation) issue); 
        
        Instant now = Instant.now();
        
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now);
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.plus(1, ChronoUnit.MINUTES));
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES));
        
        dataValidationIssue.save();
        
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(2, ChronoUnit.MINUTES));
        
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now);
        dataValidationIssue.save();
        
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(2);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getStartTime()).isEqualTo(now.plus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(1).getEndTime()).isEqualTo(now.plus(2, ChronoUnit.MINUTES));
        
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now);
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.minus(300, ChronoUnit.MINUTES));
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES));
        dataValidationIssue.save();
        
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(2, ChronoUnit.MINUTES));
        
        dataValidationIssue.addNotEstimatedBlock(channel, readingType, now.minus(1, ChronoUnit.MINUTES));
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.plus(300, ChronoUnit.MINUTES));
        dataValidationIssue.removeNotEstimatedBlock(channel, readingType, now.plus(1, ChronoUnit.MINUTES));
        dataValidationIssue.save();
        
        assertThat(dataValidationIssue.getNotEstimatedBlocks()).hasSize(1);
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getStartTime()).isEqualTo(now.minus(1, ChronoUnit.MINUTES));
        assertThat(dataValidationIssue.getNotEstimatedBlocks().get(0).getEndTime()).isEqualTo(now.plus(1, ChronoUnit.MINUTES));
    }
}
