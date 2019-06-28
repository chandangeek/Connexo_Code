/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueServiceCall extends Issue {

    ServiceCall getServiceCall();

    DefaultState getNewState();

    void setServiceCall(ServiceCall serviceCall);

    void setNewState(DefaultState newState);
}
