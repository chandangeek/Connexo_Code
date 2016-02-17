package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.History;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ServiceCategoryImpl implements ServiceCategory {

    enum Fields {
        CUSTOMPROPERTYSETUSAGE("serviceCategoryCustomPropertySetUsages");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

	//persistent fields
	private ServiceKind kind;
	private String aliasName;
	private String description;
    private boolean active;
	@SuppressWarnings("unused")
	private long version;
	@SuppressWarnings("unused")
	private Instant createTime;
	@SuppressWarnings("unused")
	private Instant modTime;
	@SuppressWarnings("unused")
	private String userName;

    private final DataModel dataModel;
    private final Thesaurus thesaurus;
    private final Provider<UsagePointImpl> usagePointFactory;

    private List<ServiceCategoryCustomPropertySetUsage> serviceCategoryCustomPropertySetUsages = new ArrayList<>();

    @Inject
	ServiceCategoryImpl(DataModel dataModel,Provider<UsagePointImpl> usagePointFactory, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.usagePointFactory = usagePointFactory;
    }
	
	ServiceCategoryImpl init(ServiceKind kind) {
		this.kind = kind;
        return this;
	}

	public ServiceKind getKind() {	
		return kind;
	}
	
	public long getId() {
		return kind.ordinal() + 1;
	}

	@Override
	public String getName() {
		return kind.getDisplayName(thesaurus);
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

    public UsagePointBuilder newUsagePoint(String mRid) {
		return new UsagePointBuilderImpl(dataModel, mRid, this);
	}
    
    @Override
    public String getTranslationKey() {
        return ServiceKind.getTranslationKey(this.kind);
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
    public List<RegisteredCustomPropertySet> getCustomPropertySets() {
        return serviceCategoryCustomPropertySetUsages
                .stream()
                .map(ServiceCategoryCustomPropertySetUsage::getRegisteredCustomPropertySet)
                .collect(Collectors.toList());
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        if (!serviceCategoryCustomPropertySetUsages.stream().filter(e -> e.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId()).findFirst().isPresent()) {
            ServiceCategoryCustomPropertySetUsage serviceCategoryCustomPropertySetUsage = this.dataModel.getInstance(ServiceCategoryCustomPropertySetUsage.class).initialize(this, registeredCustomPropertySet);
            this.serviceCategoryCustomPropertySetUsages.add(serviceCategoryCustomPropertySetUsage);
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        serviceCategoryCustomPropertySetUsages.stream()
                .filter(f -> f.getServiceCategory().getId() == this.getId())
                .filter(f -> f.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())
                .findAny().ifPresent(serviceCategoryCustomPropertySetUsages::remove);
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    private List<ServiceCategoryCustomPropertySetUsage> getServiceCategoryCustomPropertySetUsages(){
        return dataModel.query(ServiceCategoryCustomPropertySetUsage.class).select(Where.where(ServiceCategoryCustomPropertySetUsage.Fields.SERVICECATEGORY.fieldName()).isEqualTo(this));
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
