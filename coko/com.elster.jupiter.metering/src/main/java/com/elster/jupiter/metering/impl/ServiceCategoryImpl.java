package com.elster.jupiter.metering.impl;

import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Provider;

import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

public class ServiceCategoryImpl implements ServiceCategory {
	//persistent fields
	private ServiceKind kind;
	private String aliasName;
	private String description;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private Instant createTime;
	@SuppressWarnings("unused")
	private Instant modTime;
	@SuppressWarnings("unused")
	private String userName;

    private final DataModel dataModel;
    private final Provider<UsagePointImpl> usagePointFactory;
	
    @Inject
	ServiceCategoryImpl(DataModel dataModel,Provider<UsagePointImpl> usagePointFactory) {
        this.dataModel = dataModel;
        this.usagePointFactory = usagePointFactory;
    }
	
	ServiceCategoryImpl init(ServiceKind kind) {
		this.kind = kind;
        return this;
	}

	public ServiceKind getKind() {	
		return kind;
	}
	
	public int getId() {
		return kind.ordinal() + 1;
	}

	@Override
	public String getName() {
		return kind.getDisplayName();
	}
	
	@Override
	public String getAliasName() {
		return aliasName;
	}

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
	public String getDescription() {
		return description;
	}

    public void setDescription(String description) {
        this.description = description;
    }

    public void persist() {
		dataModel.persist(this);
	}

    public void update() {
        dataModel.update(this);
    }

    public UsagePoint newUsagePoint(String mRid) {
		return usagePointFactory.get().init(mRid,this);
	}
    
    public UsagePointBuilder newUsagePointBuilder() {
		return usagePointFactory.get().getNewBuilder(this);
	}

    @Override
    public String getTranslationKey() {
        return "service.category." + kind.name().toLowerCase();
    }

    @Override
    public UsagePointDetail newUsagePointDetail(UsagePoint usagePoint, Instant start) {
    	Interval interval = Interval.of(Range.atLeast(start));
        if (kind.equals(ServiceKind.ELECTRICITY)) {
            return ElectricityDetailImpl.from(dataModel, usagePoint, interval);
        } else if (kind.equals(ServiceKind.GAS)) {
            return GasDetailImpl.from(dataModel, usagePoint, interval);
        } else if (kind.equals(ServiceKind.WATER)) {
            return WaterDetailImpl.from(dataModel, usagePoint, interval);
        } else {
            return DefaultDetailImpl.from(dataModel, usagePoint, interval);
        }
    }

    @Override
    public long getVersion() {
        return version;
    }

    @Override
    public Instant getCreateTime() {
        return createTime;
    }

    @Override
    public Instant getModTime() {
        return modTime;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    History<? extends ServiceCategory> getHistory() {
        return new History<>(dataModel.mapper(ServiceCategory.class).getJournal(this.getKind()), this);
    }
}
