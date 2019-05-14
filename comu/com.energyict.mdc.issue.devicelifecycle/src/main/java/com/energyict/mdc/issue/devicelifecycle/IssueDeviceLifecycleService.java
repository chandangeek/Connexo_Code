/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.devicelifecycle;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.IssueFilter;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.nls.Thesaurus;

import aQute.bnd.annotation.ProviderType;

import java.util.Optional;

@ProviderType
public interface IssueDeviceLifecycleService {

    String COMPONENT_NAME = "IDL";
    String ISSUE_TYPE_NAME = "devicelifecycle";
    String DEVICE_LIFECYCLE_ISSUE_PREFIX = "DLI";
    String DEVICELIFECYCLE_ISSUE_REASON = "reason.device.lifecycle.transition.failure";

    Optional<? extends IssueDeviceLifecycle> findIssue(long id);

    Optional<? extends IssueDeviceLifecycle> findAndLockIssueDeviceLifecycleByIdAndVersion(long id, long version);

    Optional<OpenIssueDeviceLifecycle> findOpenIssue(long id);

    Optional<HistoricalIssueDeviceLifecycle> findHistoricalIssue(long id);

    OpenIssueDeviceLifecycle createIssue(OpenIssue baseIssue, IssueEvent issueEvent);
    
    Finder<? extends IssueDeviceLifecycle> findAllDeviceLifecycleIssues(DeviceLifecycleIssueFilter filter);

    Thesaurus thesaurus();

    Finder<? extends IssueDeviceLifecycle> findIssues(DeviceLifecycleIssueFilter filter, Class<?>... eagers);
}
