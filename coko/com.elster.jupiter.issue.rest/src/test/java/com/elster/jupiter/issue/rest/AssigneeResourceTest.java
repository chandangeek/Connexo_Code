package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.request.RequestHelper;
import com.elster.jupiter.issue.share.entity.AssigneeType;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ASSIGNEE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class AssigneeResourceTest extends IssueRestApplicationJerseyTest {
    
    @Test
    public void testGetAllAssigneesWithoutLike() {
        Query<User> queryUser = mock(Query.class);
        when(queryUser.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(new ArrayList<User>());
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
        User user = mockUser(13L, "user");
        when(securityContext.getUserPrincipal()).thenReturn(user);
        
        Map<String, Object> map = target("/assignees").queryParam(RequestHelper.ME, "true").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        List<?> assigneesList = (List<?>) map.get("data");
        assertThat(assigneesList).hasSize(1);

        Map<?,?> me = (Map<?, ?>) assigneesList.get(0);

        assertThat(me.get("id")).isEqualTo(13);
        assertThat(me.get("type")).isEqualTo(IssueAssignee.Types.USER);
        assertThat(me.get("name")).isEqualTo("user");
    }
    
    @Test
    public void testGetAllAssigneesWithLike() {
        List<User> users = Arrays.asList(mockUser(3L, "dream user"));
        Query<User> queryUser = mock(Query.class);
        when(queryUser.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(users);
        when(userService.getUserQuery()).thenReturn(queryUser);
        
        Map<String, Object> map = target("/assignees").queryParam(RequestHelper.LIKE, "dream").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        List<?> assigneesList = (List<?>) map.get("data");
        assertThat(assigneesList).hasSize(1);
        
        Map<?,?> assignee = (Map<?,?>) assigneesList.get(0);

        assertThat(assignee.get("id")).isEqualTo(3);
        assertThat(assignee.get("name")).isEqualTo("dream user");
        assertThat(assignee.get("type")).isEqualTo(IssueAssignee.Types.USER);
    }
    
    @Test
    public void testGetAssignee() {
        IssueAssignee issueAssignee = getDefaultAssignee();
        when(issueService.findIssueAssignee(AssigneeType.USER, 1)).thenReturn(Optional.of(issueAssignee));

        Map<String, Object> map = target("/assignees/1").queryParam(ASSIGNEE_TYPE, IssueAssignee.Types.USER).request().get(Map.class);

        Map<?,?> assigneeMap = (Map<?,?>) map.get("data");
        assertThat(assigneeMap).hasSize(3);
        assertThat(assigneeMap.get("id")).isEqualTo(1);
        assertThat(assigneeMap.get("name")).isEqualTo("Admin");
        assertThat(assigneeMap.get("type")).isEqualTo(IssueAssignee.Types.USER);
    }

    @Test
    public void testGetAssigneeWithoutType() {
        when(issueService.findIssueAssignee(null, 1)).thenReturn(Optional.empty());

        Response response = target("/assignees/1").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAssigneeWithIncorrectType() {
        when(issueService.findIssueAssignee(null, 1)).thenReturn(Optional.empty());
        
        Response response = target("/assignees/1").queryParam(ASSIGNEE_TYPE, "some").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetUnexistingAssignee() {
        when(issueService.findIssueAssignee(AssigneeType.USER, 1)).thenReturn(Optional.empty());
        Response response = target("/assignees/1").queryParam(ASSIGNEE_TYPE, IssueAssignee.Types.USER).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetUsersWOLike() {
        List<User> list = new ArrayList<>();
        list.add(mockUser(1, "Admin"));
        list.add(mockUser(2, "Admiral"));

        Query<User> query = mock(Query.class);
        when(query.select(any(Condition.class), any(Order[].class))).thenReturn(list);
        when(userService.getUserQuery()).thenReturn(query);

        Map<String, Object> map = target("/assignees/users").queryParam(LIKE, "ad").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
        List<?> data = (List<?>) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map<?, ?>) data.get(0)).get("id")).isEqualTo(1);
        assertThat(((Map<?, ?>) data.get(0)).get("type")).isEqualTo(IssueAssignee.Types.USER);
    }

    @Test
    public void testGetUsers() {
        List<User> list = new ArrayList<>();
        list.add(mockUser(1, "Admin"));
        list.add(mockUser(2, "Simple"));

        Query<User> query = mock(Query.class);
        when(query.select(any(Condition.class), any(Order[].class))).thenReturn(list);
        when(userService.getUserQuery()).thenReturn(query);

        Map<String, Object> map = target("/assignees/users").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
        List<?> data = (List<?>) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map<?, ?>) data.get(0)).get("id")).isEqualTo(1);
        assertThat(((Map<?, ?>) data.get(0)).get("type")).isEqualTo(IssueAssignee.Types.USER);
    }
}
