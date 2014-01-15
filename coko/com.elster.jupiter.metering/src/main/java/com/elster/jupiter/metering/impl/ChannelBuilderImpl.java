package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.orm.DataModel;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChannelBuilderImpl implements ChannelBuilder {

    private MeterActivation meterActivation;
    private List<ReadingType> readingTypes = new ArrayList<>();
    private final DataModel dataModel;

    @Inject
    public ChannelBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public ChannelBuilder meterActivation(MeterActivation meterActivation) {
        this.meterActivation = meterActivation;
        return this;
    }

    @Override
    public ChannelBuilder readingTypes(ReadingType main, ReadingType... readingTypes) {
        this.readingTypes.add(main);
        this.readingTypes.addAll(Arrays.asList(readingTypes));
        return this;
    }

    @Override
    public Channel build() {
        ChannelImpl channel = ChannelImpl.from(dataModel, meterActivation);
        channel.init(buildReadingTypes());
        return channel;
    }
    
    private List<ReadingType> buildReadingTypes() {
    	if (readingTypes.size() != 1) {
    		return readingTypes;
    	}
    	ReadingType readingType = readingTypes.get(0);
    	if  (!readingType.isRegular() || (
    			readingType.getAccumulation() != Accumulation.BULKQUANTITY && readingType.getAccumulation() != Accumulation.SUMMATION)) {
    		return readingTypes;
    	}
    	// special case of cumulative meter reading in load profile, store delta's in first slot
    	ReadingTypeCodeBuilder builder = ((ReadingTypeImpl) readingType).builder();
    	builder.accumulate(Accumulation.DELTADELTA);
    	Optional<ReadingType> delta = dataModel.mapper(ReadingType.class).getOptional(builder.code());
    	if (delta.isPresent()) {
    		return ImmutableList.of(delta.get(),readingType);
    	} else {
    		return readingTypes;
    	}    	
    }
}