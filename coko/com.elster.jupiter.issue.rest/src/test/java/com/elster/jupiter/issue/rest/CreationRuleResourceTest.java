package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;
import com.google.common.base.Optional;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.issue.rest.request.RequestHelper.LIMIT;
import static com.elster.jupiter.issue.rest.request.RequestHelper.START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreationRuleResourceTest extends Mocks {

    @Test
    public void testGetCreationRulesWOParams(){
        Response response = target("/creationrules").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetCreationRulesEmpty(){
        Query<CreationRule> query = mock(Query.class);
        when(query.select(Matchers.any(Condition.class), Matchers.anyInt(), Matchers.anyInt())).thenReturn(Collections.<CreationRule>emptyList());
        when(issueCreationService.getCreationRuleQuery(IssueReason.class, IssueType.class)).thenReturn(query);

        Map<String, Object> map = target("/creationrules")
                .queryParam(START, 0)
                .queryParam(LIMIT, 10).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }


    @Test
    public void testGetCreationRules(){
        List<CreationRule> rules = new ArrayList<>(2);
        rules.add(mockCreationRule(1, "rule 1"));
        rules.add(mockCreationRule(2, "rule 2"));
        Query<CreationRule> query = mock(Query.class);
        when(query.select(Matchers.any(Condition.class), Matchers.anyInt(), Matchers.anyInt())).thenReturn(rules);
        when(issueCreationService.getCreationRuleQuery(IssueReason.class, IssueType.class)).thenReturn(query);

        Map<String, Object> map = target("/creationrules")
                .queryParam(START, 0)
                .queryParam(LIMIT, 10).request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(0)).get("id")).isEqualTo(1);
        assertThat(((Map) data.get(0)).get("name")).isEqualTo("rule 1");
    }

    @Test
    public void testGetCreationRuleUnexisting(){
        when(issueCreationService.findCreationRule(9999)).thenReturn(Optional.<CreationRule>absent());
        Response response = target("/creationrules/9999").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetCreationRule(){
        CreationRule rule = getDefaultCreationRule();
        when(issueCreationService.findCreationRule(1)).thenReturn(Optional.of(rule));

        Map<String, Object> map = target("/creationrules/1").request().get(Map.class);
        assertThat(((Map)map.get("data")).get("id")).isEqualTo(1);
        assertThat(((Map)map.get("data")).get("name")).isEqualTo("Rule 1");
    }
}
