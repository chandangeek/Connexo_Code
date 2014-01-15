package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;

import javax.inject.Inject;
import java.util.List;

public class AmrSystemImpl implements AmrSystem {
	//persistent fields
	private int id;
	private String name;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;

    private final DataModel dataModel;
    private final MeteringService meteringService;

    @Inject
	AmrSystemImpl(DataModel dataModel, MeteringService meteringService) {
        this.dataModel = dataModel;
        this.meteringService = meteringService;
    }

	AmrSystemImpl init(int id , String name) {
		this.id = id;
		this.name = name;
        return this;
	}

    static AmrSystemImpl from(DataModel dataModel, int id, String name) {
        return dataModel.getInstance(AmrSystemImpl.class).init(id, name);
    }
	
	@Override
	public int getId() {	
		return id;
	}

	@Override
	public String getName() {
		return name;
	}
	
	void save() {
		dataModel.mapper(AmrSystem.class).persist(this);
	}

	@Override 
	public Meter newMeter(String amrId) {
		return MeterImpl.from(dataModel, this, amrId, null);
	}
	@Override
	public Meter newMeter(String amrId, String mRID) {
		return MeterImpl.from(dataModel, this, amrId, mRID);
	}

	@Override
	public Optional<Meter> findMeter(String amrId) {
		Condition condition = Operator.EQUAL.compare("amrSystemId", getId());
		condition = condition.and(Operator.EQUAL.compare("amrId",amrId));
		List<Meter> candidates = meteringService.getMeterQuery().select(condition);
		switch(candidates.size()) {
			case 0:
				return Optional.absent();
			case 1:
				return Optional.of(candidates.get(0));
			default:
				throw new IllegalStateException();
		}
	}

}
