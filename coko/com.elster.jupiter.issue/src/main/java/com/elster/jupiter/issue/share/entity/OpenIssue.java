package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface OpenIssue extends Issue {
    
    HistoricalIssue close(IssueStatus status);

    default HistoricalIssue closeInternal(IssueStatus status){
        throw new UnsupportedOperationException();
    }
}
