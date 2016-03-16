package com.elster.jupiter.issue.rest.response.issue;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.rest.util.InfoFactory;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface IssueInfoFactoryService {

    InfoFactory getInfoFactoryFor(Issue issue);
}
