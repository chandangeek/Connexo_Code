package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.templates.EventAggregationRuleTemplate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EventAggregationRuleTemplateTest extends BaseTest {

    @Test
    @Transactional
    public void testCanCreateIssue() {
        CreationRule rule = getCreationRule("testCanCreateIssue", ModuleConstants.REASON_POWER_OUTAGE);
        Meter meter = createMeter("1", "Name");
        OpenIssue baseIssue = getBaseIssue(rule, meter);

        EventAggregationRuleTemplate template = getInjector().getInstance(EventAggregationRuleTemplate.class);
        DataCollectionEvent event = mock(DataCollectionEvent.class);

        assertThat(template.createIssue(baseIssue, event)).isNotNull();
    }
    
    private Meter createMeter(String amrId, String name) {
        AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
        return amrSystem.newMeter(amrId, name).create();
    }

    private OpenIssue getBaseIssue(CreationRule rule, Meter meter) {
        OpenIssueImpl baseIssue = getIssueDataModel().getInstance(OpenIssueImpl.class);
        baseIssue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).get());
        baseIssue.setReason(rule.getReason());
        baseIssue.setDevice(meter);
        baseIssue.setRule(rule);
        baseIssue.setPriority(Priority.DEFAULT);
        baseIssue.save();
        return baseIssue;
    }
}
