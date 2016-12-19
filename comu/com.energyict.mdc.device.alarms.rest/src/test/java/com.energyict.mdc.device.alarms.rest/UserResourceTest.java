package com.energyict.mdc.device.alarms.rest;


import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.RequestHelper;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserResourceTest extends DeviceAlarmApplicationTest{

    @Mock
    IssueAssignee issueAssignee;

    @Test
    public void testGetAllAssigneesWithoutLike() {
        this.mockTranslation(com.elster.jupiter.issue.rest.TranslationKeys.ISSUE_ASSIGNEE_UNASSIGNED);
        Query<User> queryUser = mock(Query.class);
        when(queryUser.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(new ArrayList<>());
        when(userService.getUserQuery()).thenReturn(queryUser);
        Map<String, Object> map = target("/assignees").queryParam("start", 0).queryParam("limit", 10).request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        List<?> assigneesList = (List<?>) map.get("data");
        assertThat(assigneesList).hasSize(1);

        Map<?,?> unassigned = (Map<?, ?>) assigneesList.get(0);

        assertThat(unassigned.get("id")).isEqualTo(-1);
        assertThat(unassigned.get("type")).isEqualTo("UnexistingType");
        assertThat(unassigned.get("name")).isEqualTo("Unassigned");
    }

    @Test
    public void testGetAllAssigneesWithMeParameter() {
        User user = mockUser(13L, "Admin");
        when(securityContext.getUserPrincipal()).thenReturn(user);

        Map<String, Object> map = target("/assignees").queryParam(RequestHelper.ME, "true").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        List<?> assigneesList = (List<?>) map.get("data");
        assertThat(assigneesList).hasSize(1);

        Map<?,?> me = (Map<?, ?>) assigneesList.get(0);

        assertThat(me.get("id")).isEqualTo(13);
        assertThat(me.get("name")).isEqualTo("Admin");
    }

    @Test
    public void testGetAllAssigneesWithLike() {
        List<User> users = Arrays.asList(mockUser(3L, "Admin"));
        Query<User> queryUser = mock(Query.class);
        when(queryUser.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(users);
        when(userService.getUserQuery()).thenReturn(queryUser);

        Map<String, Object> map = target("/assignees").queryParam(RequestHelper.LIKE, "dream").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        List<?> assigneesList = (List<?>) map.get("data");
        assertThat(assigneesList).hasSize(1);

        Map<?,?> assignee = (Map<?,?>) assigneesList.get(0);

        assertThat(assignee.get("id")).isEqualTo(3);
        assertThat(assignee.get("name")).isEqualTo("Admin");
    }

    @Test
    public void testGetUnexistingAssignee() {
        when(issueService.findIssueAssignee(1L, null)).thenReturn(issueAssignee);
        Response response = target("/assignees/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

}
