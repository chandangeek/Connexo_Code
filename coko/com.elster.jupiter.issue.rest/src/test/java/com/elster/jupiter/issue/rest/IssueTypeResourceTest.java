package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.util.conditions.Condition;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueTypeResourceTest extends IssueRestApplicationJerseyTest {

    @Test
    public void testgetIssueTypesEmpty(){
        Query<IssueType> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject())).thenReturn(Collections.<IssueType>emptyList());
        when(issueService.query(IssueType.class)).thenReturn(query);


        Map<String, Object> map = target("/issuetypes").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    @Test
    public void testgetIssueTypes(){
        IssueType type = getDefaultIssueType();
        Query<IssueType> query = mock(Query.class);
        when(query.select(Matchers.<Condition>anyObject())).thenReturn(Collections.singletonList(type));
        when(issueService.query(IssueType.class)).thenReturn(query);


        Map<String, Object> map = target("/issuetypes").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(1);
        List data = (List) map.get("data");
        assertThat(data).hasSize(1);
        assertThat(((Map) data.get(0)).get("uid")).isEqualTo(type.getKey());
    }
}
