package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;

public class ServiceCategoryImpl implements ServiceCategory {
	//persistent fields
	private ServiceKind kind;
	private String aliasName;
	private String description;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private UtcInstant createTime;
	@SuppressWarnings("unused")
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;

    private final DataModel dataModel;
	
    @Inject
	ServiceCategoryImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }
	
	ServiceCategoryImpl init(ServiceKind kind) {
		this.kind = kind;
        return this;
	}

    static ServiceCategoryImpl from(DataModel dataModel, ServiceKind serviceKind) {
        return dataModel.getInstance(ServiceCategoryImpl.class).init(serviceKind);
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
		dataModel.mapper(ServiceCategory.class).persist(this);
	}
	
	public UsagePoint newUsagePoint(String mRid) {
		return UsagePointImpl.from(dataModel, mRid,this);
	}
}
