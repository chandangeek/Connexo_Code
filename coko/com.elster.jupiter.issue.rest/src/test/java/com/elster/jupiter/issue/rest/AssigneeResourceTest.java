package com.elster.jupiter.issue.rest;

import static com.elster.jupiter.issue.rest.request.RequestHelper.ASSIGNEE_TYPE;
import static com.elster.jupiter.issue.rest.request.RequestHelper.LIKE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.*;
import java.util.regex.Matcher;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.mockito.Matchers;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.rest.i18n.MessageSeeds;
import com.elster.jupiter.issue.rest.request.RequestHelper;
import com.elster.jupiter.issue.share.entity.AssigneeRole;
import com.elster.jupiter.issue.share.entity.AssigneeTeam;
import com.elster.jupiter.issue.share.entity.IssueAssignee;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;

@SuppressWarnings("unchecked")
public class AssigneeResourceTest extends Mocks {
    
    @Test
    public void testGetAllAssigneesWithoutLike() {
        User user = mockUser(13L, "user");
        when(securityContext.getUserPrincipal()).thenReturn(user);
        
        when(thesaurus.getString(MessageSeeds.ISSUE_ASSIGNEE_ME.getKey(), MessageSeeds.ISSUE_ASSIGNEE_ME.getDefaultFormat())).thenReturn("Me");
        when(thesaurus.getString(MessageSeeds.ISSUE_ASSIGNEE_UNASSIGNED.getKey(), MessageSeeds.ISSUE_ASSIGNEE_UNASSIGNED.getDefaultFormat())).thenReturn("Unassigned");

        Map<String, Object> map = target("/assignees").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
        List<?> assigneesList = (List<?>) map.get("data");
        assertThat(assigneesList).hasSize(2);
        
        Map<?,?> unassigned = (Map<?, ?>) assigneesList.get(0);
        Map<?,?> me = (Map<?, ?>) assigneesList.get(1);
        
        assertThat(unassigned.get("id")).isEqualTo(-1);
        assertThat(unassigned.get("type")).isEqualTo("UnexistingType");
        assertThat(unassigned.get("name")).isEqualTo("Unassigned");
        
        assertThat(me.get("id")).isEqualTo(13);
        assertThat(me.get("type")).isEqualTo(IssueAssignee.Types.USER);
        assertThat(me.get("name")).isEqualTo("Me");
    }
    
    @Test
    public void testGetAllAssigneesWithLike() {
        List<AssigneeTeam> teams = Arrays.asList(mockTeam(1L, "dream team"));
        List<AssigneeRole> roles = Arrays.asList(mockRole(2L, "dream role"));
        List<User> users = Arrays.asList(mockUser(3L, "dream user"));
        
        Query<AssigneeTeam> queryTeam = mock(Query.class);
        when(queryTeam.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(teams);
        when(issueService.query(AssigneeTeam.class)).thenReturn(queryTeam);
        
        Query<AssigneeRole> queryRole = mock(Query.class);
        when(queryRole.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(roles);
        when(issueService.query(AssigneeRole.class)).thenReturn(queryRole);

        Query<User> queryUser = mock(Query.class);
        when(queryUser.select(Matchers.<Condition>anyObject(), Matchers.<Order>anyObject())).thenReturn(users);
        when(userService.getUserQuery()).thenReturn(queryUser);
        
        Map<String, Object> map = target("/assignees").queryParam(RequestHelper.LIKE, "dream").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(3);
        List<?> assigneesList = (List<?>) map.get("data");
        assertThat(assigneesList).hasSize(3);
        
        Map<?,?> firstAssignee = (Map<?,?>) assigneesList.get(0);
        Map<?,?> secondAssignee = (Map<?,?>) assigneesList.get(1);
        Map<?,?> thirdAssignee = (Map<?,?>) assigneesList.get(2);
        
        assertThat(firstAssignee.get("id")).isEqualTo(1);
        assertThat(firstAssignee.get("name")).isEqualTo("dream team");
        assertThat(firstAssignee.get("type")).isEqualTo(IssueAssignee.Types.GROUP);

        assertThat(secondAssignee.get("id")).isEqualTo(2);
        assertThat(secondAssignee.get("name")).isEqualTo("dream role");
        assertThat(secondAssignee.get("type")).isEqualTo(IssueAssignee.Types.ROLE);
        
        assertThat(thirdAssignee.get("id")).isEqualTo(3);
        assertThat(thirdAssignee.get("name")).isEqualTo("dream user");
        assertThat(thirdAssignee.get("type")).isEqualTo(IssueAssignee.Types.USER);
    }
    
    @Test
    public void testGetAssignee() {
        IssueAssignee issueAssignee = getDefaultAssignee();
        when(issueService.findIssueAssignee(IssueAssignee.Types.USER, 1)).thenReturn(issueAssignee);

        Map<String, Object> map = target("/assignees/1").queryParam(ASSIGNEE_TYPE, IssueAssignee.Types.USER).request().get(Map.class);

        Map<?,?> assigneeMap = (Map<?,?>) map.get("data");
        assertThat(assigneeMap).hasSize(3);
        assertThat(assigneeMap.get("id")).isEqualTo(1);
        assertThat(assigneeMap.get("name")).isEqualTo("Admin");
        assertThat(assigneeMap.get("type")).isEqualTo(IssueAssignee.Types.USER);
    }

    @Test
    public void testGetAssigneeWithoutType() {
        IssueAssignee issueAssignee = getDefaultAssignee();
        when(issueService.findIssueAssignee(IssueAssignee.Types.USER, 1)).thenReturn(issueAssignee);

        Response response = target("/assignees/1").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetAssigneeWithIncorrectType() {
        IssueAssignee issueAssignee = getDefaultAssignee();
        when(issueService.findIssueAssignee(IssueAssignee.Types.USER, 1)).thenReturn(issueAssignee);

        Response response = target("/assignees/1").queryParam(ASSIGNEE_TYPE, IssueAssignee.Types.GROUP).request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetUnexistingAssignee() {
        when(issueService.findIssueAssignee(IssueAssignee.Types.USER, 1)).thenReturn(null);
        Response response = target("/assignees/1").queryParam(ASSIGNEE_TYPE, IssueAssignee.Types.USER).request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetGroups() {
        List<AssigneeTeam> teams = Arrays.asList(mockTeam(1L, "Team"));

        Query<AssigneeTeam> query = mock(Query.class);
        when(query.select(Condition.TRUE)).thenReturn(teams);
        when(issueService.query(AssigneeTeam.class)).thenReturn(query);

        Map<String, Object> map = target("/assignees/groups").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List<?>) map.get("data")).hasSize(1);
        Map<?,?> team = (Map<?,?>) ((List<?>) map.get("data")).get(0);
        assertThat(team.get("id")).isEqualTo(1);
        assertThat(team.get("type")).isEqualTo(IssueAssignee.Types.GROUP);
        assertThat(team.get("name")).isEqualTo("Team");
    }

    @Test
    public void testNoAssigneeGroups() {
        Query<AssigneeTeam> query = mock(Query.class);
        when(query.select(Condition.TRUE)).thenReturn(Collections.EMPTY_LIST);
        when(issueService.query(AssigneeTeam.class)).thenReturn(query);
        Map<String, Object> map = target("/assignees/groups").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List<?>) map.get("data")).hasSize(0);
    }
    
    @Test
    public void testGetRoles() {
        List<AssigneeRole> roles = Arrays.asList(mockRole(1L, "Role"));

        Query<AssigneeRole> query = mock(Query.class);
        when(query.select(Condition.TRUE)).thenReturn(roles);
        when(issueService.query(AssigneeRole.class)).thenReturn(query);

        Map<String, Object> map = target("/assignees/roles").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(1);
        assertThat((List<?>) map.get("data")).hasSize(1);
        Map<?,?> team = (Map<?,?>) ((List<?>) map.get("data")).get(0);
        assertThat(team.get("id")).isEqualTo(1);
        assertThat(team.get("type")).isEqualTo(IssueAssignee.Types.ROLE);
        assertThat(team.get("name")).isEqualTo("Role");
    }
    
    @Test
    public void testNoAssigneeRoles() {
        Query<AssigneeRole> query = mock(Query.class);
        when(query.select(Condition.TRUE)).thenReturn(Collections.EMPTY_LIST);
        when(issueService.query(AssigneeRole.class)).thenReturn(query);
        Map<String, Object> map = target("/assignees/roles").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List<?>) map.get("data")).hasSize(0);
    }

    @Test
    public void testGetUsersWOLike() {
        List<User> list = new ArrayList<>();
        list.add(mockUser(1, "Admin"));
        list.add(mockUser(2, "Admiral"));

        Query<User> query = mock(Query.class);
        when(query.select(Matchers.any(Condition.class), Matchers.any(Order[].class))).thenReturn(list);
        when(userService.getUserQuery()).thenReturn(query);

        Map<String, Object> map = target("/assignees/users").queryParam(LIKE, "ad").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
        List<?> data = (List<?>) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(0)).get("id")).isEqualTo(1);
        assertThat(((Map) data.get(0)).get("type")).isEqualTo(IssueAssignee.Types.USER);
    }

    @Test
    public void testGetUsers() {
        List<User> list = new ArrayList<>();
        list.add(mockUser(1, "Admin"));
        list.add(mockUser(2, "Simple"));

        Query<User> query = mock(Query.class);
        when(query.select(Matchers.any(Condition.class), Matchers.any(Order[].class))).thenReturn(list);
        when(userService.getUserQuery()).thenReturn(query);

        Map<String, Object> map = target("/assignees/users").request().get(Map.class);

        assertThat(map.get("total")).isEqualTo(2);
        List<?> data = (List<?>) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(0)).get("id")).isEqualTo(1);
        assertThat(((Map) data.get(0)).get("type")).isEqualTo(IssueAssignee.Types.USER);
    }
}
