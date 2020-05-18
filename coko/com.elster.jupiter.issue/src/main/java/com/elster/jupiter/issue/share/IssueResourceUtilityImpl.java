package com.elster.jupiter.issue.share;

import com.elster.jupiter.issue.share.entity.*;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import org.osgi.service.component.annotations.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component(name = "com.elster.jupiter.issue.share", service = IssueResourceUtility.class, immediate = true)
public class IssueResourceUtilityImpl implements IssueResourceUtility {
    @Override
    public List<IssueGroupInfo> getIssueGroupList(List<? extends Issue> issues, String value) {
        List<IssueGroupInfo> infos = new ArrayList<>();
        Map<?, ? extends List<?>> l = null;
        switch (value) {
            case "issueType":
                l = issues.stream().collect(Collectors.groupingBy(Issue::getType));
                for (int i = 0; i < l.keySet().size(); i++) {
                    IssueType issueType = (IssueType) l.keySet().toArray()[i];
                    infos.add(new IssueGroupInfo(issueType.getKey(), issueType.getName(), ((List) (l.values().toArray()[i])).size()));
                }
                break;
            case "status":
                l = issues.stream().collect(Collectors.groupingBy(Issue::getStatus));
                for (int i = 0; i < l.keySet().size(); i++) {
                    IssueStatus issueStatus = (IssueStatus) l.keySet().toArray()[i];
                    infos.add(new IssueGroupInfo(issueStatus.getKey(), issueStatus.getName(), ((List) (l.values().toArray()[i])).size()));
                }
                break;
            case "reason":
                l = issues.stream().collect(Collectors.groupingBy(Issue::getReason));
                for (int i = 0; i < l.keySet().size(); i++) {
                    IssueReason issueReason = (IssueReason) l.keySet().toArray()[i];
                    infos.add(new IssueGroupInfo(issueReason.getKey(), issueReason.getName(), ((List) (l.values().toArray()[i])).size()));
                }
                break;
            case "userAssignee":
                l = issues.stream().collect(Collectors.groupingBy(Issue::getAssignee));
                List<User> users = new ArrayList<>();
                for (int i = 0; i < l.keySet().size(); i++) {
                    users.add(((IssueAssignee) l.keySet().toArray()[i]).getUser());
                }
                long userUnassignedCount = users.stream().filter(item -> item == null).count();
                Map<?, ? extends List<?>> map = users.stream().filter(item -> item != null).collect(Collectors.groupingBy(User::getName));
                for (int i = 0; i < map.keySet().size(); i++) {
                    long workGroupId = ((User)((List) (map.values().toArray()[i])).get(0)).getId();
                    String description = (String) map.keySet().toArray()[i];
                    infos.add(new IssueGroupInfo(workGroupId, description, ((List) (map.values().toArray()[i])).size()));

                }
                if (userUnassignedCount != 0)
                    infos.add(new IssueGroupInfo(-1l, "Unassigned", userUnassignedCount));
                break;
            case "workGroupAssignee":
                l = issues.stream().collect(Collectors.groupingBy(Issue::getAssignee));
                List<WorkGroup> workGroups = new ArrayList<>();
                for (int i = 0; i < l.keySet().size(); i++) {
                    workGroups.add(((IssueAssignee) l.keySet().toArray()[i]).getWorkGroup());
                }
                long workgroupUnassignedCount = workGroups.stream().filter(item -> item == null).count();
                Map<?, ? extends List<?>> map1 = workGroups.stream().filter(item -> item != null && item.getDescription()!=null).
                        collect(Collectors.groupingBy(WorkGroup::getDescription));
                for (int i = 0; i < map1.keySet().size(); i++) {
                    long workGroupId = ((WorkGroup)((List) (map1.values().toArray()[i])).get(0)).getId();
                    String description = (String) map1.keySet().toArray()[0];
                    infos.add(new IssueGroupInfo(workGroupId, description, ((List) (map1.values().toArray()[i])).size()));
                }
                if (workgroupUnassignedCount != 0)
                    infos.add(new IssueGroupInfo(-1l, "Unassigned", workgroupUnassignedCount));
                break;
            default:
                break;
        }
        return infos;
    }
}
