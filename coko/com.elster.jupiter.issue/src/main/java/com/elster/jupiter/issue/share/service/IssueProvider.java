package com.elster.jupiter.issue.share.service;

import java.util.Optional;

import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.OpenIssue;

public interface IssueProvider {
    
    Optional<? extends OpenIssue> getOpenIssue(OpenIssue issue);
    
    Optional<? extends HistoricalIssue> getHistoricalIssue(HistoricalIssue issue);
    
}
