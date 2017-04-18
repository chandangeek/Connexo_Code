/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimation.rest.impl;

import com.elster.jupiter.metering.aggregation.ReadingQualityComment;
import com.elster.jupiter.metering.aggregation.ReadingQualityCommentCategory;

public class EstimationCommentInfo {

    public long id;
    public String comment;

    private EstimationCommentInfo(long id, String comment){
        this.id = id;
        this.comment = comment;
    }

    public static EstimationCommentInfo from(ReadingQualityComment readingQualityComment) {
        if (readingQualityComment.getCommentCategory().equals(ReadingQualityCommentCategory.ESTIMATION)) {
            return new EstimationCommentInfo(readingQualityComment.getId(), readingQualityComment.getComment());
        }

        throw new IllegalArgumentException("Invalid comment category. Must be an estimation comment.");
    }
}
