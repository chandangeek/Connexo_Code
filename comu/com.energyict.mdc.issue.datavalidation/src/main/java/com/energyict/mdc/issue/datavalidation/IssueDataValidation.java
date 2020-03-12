/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.issue.share.entity.HasLastSuspectOccurrenceDatetime;
import com.elster.jupiter.issue.share.entity.HasTotalOccurrencesNumber;
import com.elster.jupiter.issue.share.entity.Issue;

import java.util.List;

@ProviderType
public interface IssueDataValidation extends Issue, HasTotalOccurrencesNumber, HasLastSuspectOccurrenceDatetime {

    List<NotEstimatedBlock> getNotEstimatedBlocks();

}
