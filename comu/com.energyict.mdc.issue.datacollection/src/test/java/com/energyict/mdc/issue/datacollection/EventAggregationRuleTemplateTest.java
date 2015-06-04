package com.energyict.mdc.issue.datacollection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;
import com.energyict.mdc.issue.datacollection.impl.templates.EventAggregationRuleTemplate;

public class EventAggregationRuleTemplateTest extends BaseTest {

    @Test
    @Transactional
    public void testCanCreateIssue() {
        CreationRule rule = getCreationRule("testCanCreateIssue", ModuleConstants.REASON_CONNECTION_FAILED);
        Meter meter = createMeter("1", "mrid");
        Issue baseIssue = getBaseIssue(rule, meter);

        EventAggregationRuleTemplate template = getInjector().getInstance(EventAggregationRuleTemplate.class);
        DataCollectionEvent event = mock(DataCollectionEvent.class);

        assertThat(template.createIssue(baseIssue, event).isPresent()).isTrue();
    }
    
    private Meter createMeter(String amrId, String mrid) {
        AmrSystem amrSystem = getMeteringService().findAmrSystem(1).get();
        Meter meter = amrSystem.newMeter(amrId, mrid);
        meter.save();
        return meter;
    }

    private Issue getBaseIssue(CreationRule rule, Meter meter) {
        DataModel isuDataModel = getIssueDataModel();
        Issue baseIssue = isuDataModel.getInstance(OpenIssueImpl.class);
        baseIssue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).get());
        baseIssue.setReason(rule.getReason());
        baseIssue.setDevice(meter);
        baseIssue.setRule(rule);
        return baseIssue;
    }
}
