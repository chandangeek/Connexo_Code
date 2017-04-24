/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.aggregation.ReadingQualityCommentCategory;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface ReadingQualityComment {

    ReadingQualityCommentCategory getCommentCategory();

    String getComment();

    long getId();
}
