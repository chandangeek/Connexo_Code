/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface IssueActionFactory {

    String getId();

    IssueAction createIssueAction(String issueActionImpl);
}
