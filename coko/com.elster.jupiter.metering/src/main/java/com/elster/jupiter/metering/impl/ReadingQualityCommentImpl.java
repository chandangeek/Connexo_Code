/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.ReadingQualityComment;
import com.elster.jupiter.metering.aggregation.ReadingQualityCommentCategory;

public class ReadingQualityCommentImpl implements ReadingQualityComment {

    //managed by orm
    private long id;

    private ReadingQualityCommentCategory category;
    private String comment;

    public ReadingQualityComment init(String comment, ReadingQualityCommentCategory category) {
        this.category = category;
        this.comment = comment;
        return this;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public ReadingQualityCommentCategory getCommentCategory() {
        return this.category;
    }

    @Override
    public long getId() {
        return this.id;
    }
}
