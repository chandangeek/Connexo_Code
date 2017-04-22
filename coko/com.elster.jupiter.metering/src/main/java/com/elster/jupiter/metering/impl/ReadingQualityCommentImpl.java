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

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String getComment() {
        return this.comment;
    }

    @Override
    public void setCommentCategory(ReadingQualityCommentCategory category) {
        this.category = category;
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
