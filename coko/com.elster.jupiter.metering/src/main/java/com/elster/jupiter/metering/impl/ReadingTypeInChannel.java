/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import javax.inject.Inject;
import java.util.List;

public class ReadingTypeInChannel extends AbstractCimChannel {

    @SuppressWarnings("unused")
    private int position;
    
    private final Reference<ChannelImpl> channel = ValueReference.absent();
    private final Reference<IReadingType> readingType = ValueReference.absent();
    private DerivationRule derivationRule;

    @Inject
    ReadingTypeInChannel(DataModel dataModel, MeteringService meteringService) {
        super(dataModel, meteringService);
    }

    private ReadingTypeInChannel init(ChannelImpl channel, IReadingType readingType, DerivationRule derivationRule) {
        this.channel.set(channel);
        this.readingType.set(readingType);
        this.derivationRule = derivationRule;
        return this;
    }

    static ReadingTypeInChannel from(DataModel dataModel, ChannelImpl channel, IReadingType readingType, DerivationRule derivationRule) {
        return dataModel.getInstance(ReadingTypeInChannel.class)
                .init(channel, readingType, derivationRule);
    }

    @Override
    public IReadingType getReadingType() {
        return readingType.get();
    }

    @Override
    public ChannelImpl getChannel() {
        return channel.get();
    }

    public DerivationRule getDerivationRule() {
        return derivationRule;
    }

    @Override
    public void removeReadings(List<? extends BaseReadingRecord> readings) {
        //TODO automatically generated method body, provide implementation.

    }

}
