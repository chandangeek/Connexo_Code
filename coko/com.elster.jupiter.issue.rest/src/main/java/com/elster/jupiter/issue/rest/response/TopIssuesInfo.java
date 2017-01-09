package com.elster.jupiter.issue.rest.response;


import com.elster.jupiter.issue.rest.response.issue.IssueInfo;
import com.elster.jupiter.issue.share.entity.Issue;

import java.util.List;
import java.util.stream.Collectors;

public class TopIssuesInfo {

    public long totalUserAssigned;
    public long totalWorkGroupAssigned;
    public long total;
    public List<IssueInfo> items;

    public TopIssuesInfo(){

    }

    public TopIssuesInfo(List<Issue> issues, long totalUserAssigned, long totalWorkGroupAssigned){
        items = issues.stream().map(IssueInfo::new).collect(Collectors.toList());
        total = items.size();
        this.totalUserAssigned = totalUserAssigned;
        this.totalWorkGroupAssigned = totalWorkGroupAssigned;
    }
}
