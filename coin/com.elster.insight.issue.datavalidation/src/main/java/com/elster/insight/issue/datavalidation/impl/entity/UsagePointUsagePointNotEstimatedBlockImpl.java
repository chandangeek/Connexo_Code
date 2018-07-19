/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl.entity;

import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.insight.issue.datavalidation.UsagePointNotEstimatedBlock;

import javax.validation.constraints.NotNull;
import java.time.Instant;

public class UsagePointUsagePointNotEstimatedBlockImpl implements UsagePointNotEstimatedBlock {
    
    public enum Fields {
        ISSUE("issue"),
        CHANNEL("channel"),
        READINGTYPE("readingType"),
        STARTTIME("startTime"),
        ENDTIME("endTime");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<Channel> channel = ValueReference.absent();
    
    @IsPresent
    private Reference<ReadingType> readingType = ValueReference.absent();

    @NotNull
    private Instant startTime;

    @NotNull
    private Instant endTime;

    UsagePointUsagePointNotEstimatedBlockImpl init(Channel channel, ReadingType readingType, Instant startTime, Instant endTime) {
        this.channel.set(channel);
        this.readingType.set(readingType);
        this.startTime = startTime;
        this.endTime = endTime;
        return this;
    }

    @Override
    public Channel getChannel() {
        return channel.get();
    }
    
    @Override
    public ReadingType getReadingType() {
        return readingType.get();
    }
    
    @Override
    public Instant getStartTime() {
        return startTime;
    }
    
    @Override
    public Instant getEndTime() {
        return endTime;
    }
}
