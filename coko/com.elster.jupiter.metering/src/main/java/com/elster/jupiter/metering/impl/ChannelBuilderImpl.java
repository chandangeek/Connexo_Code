package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.ReadingTypeCodeBuilder;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.orm.DataModel;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ChannelBuilderImpl implements ChannelBuilder {

    private MeterActivation meterActivation;
    private List<ReadingTypeImpl> readingTypes = new ArrayList<>();
    private final DataModel dataModel;
    private final Provider<ChannelImpl> channelFactory;

    @Inject
    public ChannelBuilderImpl(DataModel dataModel, Provider<ChannelImpl> channelFactory) {
        this.dataModel = dataModel;
        this.channelFactory = channelFactory;
    }

    @Override
    public ChannelBuilder meterActivation(MeterActivation meterActivation) {
        this.meterActivation = meterActivation;
        return this;
    }

    @Override
    public ChannelBuilder readingTypes(ReadingTypeImpl main, ReadingTypeImpl... readingTypes) {
        this.readingTypes.add(main);
        this.readingTypes.addAll(Arrays.asList(readingTypes));
        return this;
    }

    @Override
    public Channel build() {
        return channelFactory.get().init(meterActivation,buildReadingTypes());
        
    }
    
    private List<ReadingTypeImpl> buildReadingTypes() {
    	if (readingTypes.size() != 1) {
    		return readingTypes;
    	}
    	ReadingTypeImpl readingType = readingTypes.get(0);
    	if  (!readingType.isRegular() || !readingType.isCumulative()) {
    		return readingTypes;
    	}
    	// special case of cumulative reading type in load profile, store delta's in first slot
    	ReadingTypeCodeBuilder builder = readingType.builder();
    	builder.accumulate(Accumulation.DELTADELTA);
    	Optional<ReadingTypeImpl> delta = dataModel.mapper(ReadingTypeImpl.class).getOptional(builder.code());
    	if (delta.isPresent()) {
    		return ImmutableList.of(delta.get(),readingType);
    	} else {
    		return readingTypes;
    	}    	
    }
}