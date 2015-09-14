package com.elster.jupiter.issue.rest;

import com.elster.jupiter.issue.share.entity.IssueGroup;
import com.elster.jupiter.issue.share.service.IssueGroupFilter;
import com.elster.jupiter.transaction.TransactionContext;
import org.junit.Test;
import org.mockito.Matchers;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IssueResourceTest extends IssueRestApplicationJerseyTest {

    @Test
    public void testGroupedList() {
        IssueGroup entity = mock(IssueGroup.class);
        when(entity.getGroupKey()).thenReturn(1L);
        when(entity.getGroupName()).thenReturn("Reason 1");
        when(entity.getCount()).thenReturn(5L);

        List<IssueGroup> groupedList = Arrays.asList(entity);
        when(issueService.getIssueGroupList(Matchers.<IssueGroupFilter>anyObject())).thenReturn(groupedList);

        TransactionContext context = mock(TransactionContext.class);
        when(transactionService.getContext()).thenReturn(context);

        String filter = URLEncoder.encode("[{\"property\":\"field\",\"value\":\"reason\"},{\"property\":\"issueType\",\"value\":\"datacollection\"}]");

        Map<?, ?> map = target("issues/groupedlist")
                .queryParam("start", 0).queryParam("limit", 1).queryParam("filter", filter).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);

        List<?> groups = (List<?>) map.get("issueGroups");
        assertThat(groups).hasSize(1);

        Map<?, ?> groupMap = (Map<?, ?>) groups.get(0);
        assertThat(groupMap.get("id")).isEqualTo(1);
        assertThat(groupMap.get("reason")).isEqualTo("Reason 1");
        assertThat(groupMap.get("number")).isEqualTo(5);
    }
}
