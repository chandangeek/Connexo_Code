/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.aggregation;

public interface ReadingQualityComment {

    ReadingQualityCommentCategory getCommentCategory();

    void setCommentCategory(ReadingQualityCommentCategory commentCategory);

    String getComment();

    void setComment(String comment);

    long getId();
}
