package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.inject.Provider;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Objects.toStringHelper;

public class UsagePointImpl implements UsagePoint {
	// persistent fields
	private long id;
	private String aliasName;
	private String description;
	private String mRID;
	private String name;
	private boolean isSdp;
	private boolean isVirtual;
	private String outageRegion;
	private String readCycle;
	private String readRoute;
	private String servicePriority;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;


    private TemporalReference<UsagePointDetailImpl> detail = Temporals.absent();

    // associations
	private final Reference<ServiceCategory> serviceCategory = ValueReference.absent();
	private final Reference<ServiceLocation> serviceLocation = ValueReference.absent();
	private final List<MeterActivationImpl> meterActivations = new ArrayList<>();
	private final List<UsagePointAccountability> accountabilities = new ArrayList<>();
	
    private final DataModel dataModel;
    private final EventService eventService;
    private final Provider<MeterActivationImpl> meterActivationFactory;
    private final Provider<UsagePointAccountabilityImpl> accountabilityFactory;

    @Inject
	UsagePointImpl(DataModel dataModel, EventService eventService, 
			Provider<MeterActivationImpl> meterActivationFactory, 
			Provider<UsagePointAccountabilityImpl> accountabilityFactory) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.meterActivationFactory = meterActivationFactory;
        this.accountabilityFactory = accountabilityFactory;
    }
	
	UsagePointImpl init(String mRID , ServiceCategory serviceCategory) {
		this.mRID = mRID;
		this.serviceCategory.set(serviceCategory);
		this.isSdp = true;
        return this;
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override 
	public long getServiceLocationId() {
		ServiceLocation location = getServiceLocation();
		return location == null ? 0 : location.getId();
	}
	
	@Override
	public String getAliasName() {
		return aliasName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getMRID() {
		return mRID;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isSdp() {
		return isSdp;
	}

	@Override
	public boolean isVirtual() {
		return isVirtual;
	}

	@Override
	public String getOutageRegion() {
		return outageRegion;
	}

	@Override
	public String getReadCycle() {
		return readCycle;
	}

	@Override
	public String getReadRoute() {
		return readRoute;
	}

	@Override
	public String getServicePriority() {
		return servicePriority;
	}

	@Override
	public ServiceCategory getServiceCategory() {
		return serviceCategory.get();
	}

	@Override
	public ServiceLocation getServiceLocation() {
		return serviceLocation.get();
	}

	@Override
	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setMRID(String mRID) {
		this.mRID = mRID;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setSdp(boolean isSdp) {
		this.isSdp = isSdp;
	}

	@Override
	public void setVirtual(boolean isVirtual) {
		this.isVirtual = isVirtual;
	}

	@Override
	public void setOutageRegion(String outageRegion) {
		this.outageRegion = outageRegion;
	}

	@Override
	public void setReadCycle(String readCycle) {
		this.readCycle = readCycle;
	}

	@Override
	public void setReadRoute(String readRoute) {
		this.readRoute = readRoute;
	}

	@Override
	public void setServicePriority(String servicePriority) {
		this.servicePriority = servicePriority;
	}

	@Override
	public void setServiceLocation(ServiceLocation serviceLocation) {
		this.serviceLocation.set(serviceLocation);
	}

	@Override
	public void save() {
		if (id == 0) {
			dataModel.persist(this);
            eventService.postEvent(EventType.USAGEPOINT_CREATED.topic(), this);
		} else {
            dataModel.update(this);
            eventService.postEvent(EventType.USAGEPOINT_UPDATED.topic(), this);
		}
	}

    @Override
    public void delete() {
        dataModel.remove(this);
        eventService.postEvent(EventType.USAGEPOINT_DELETED.topic(), this);
    }

	@Override
	public List<MeterActivationImpl> getMeterActivations() {
		return ImmutableList.copyOf(meterActivations);
	}
	
	@Override
	public MeterActivation getCurrentMeterActivation() {
		for (MeterActivation each : meterActivations) {
			if (each.isCurrent()) {
				return each;
			}
		}
		return null;
	}
	
	public Date getCreateDate() {
		return createTime.toDate();
	}
	
	public Date getModificationDate() {
		return modTime.toDate(); 
	}

	public long getVersion() {
		return version;
	}
	
	@Override
	public List<UsagePointAccountability> getAccountabilities() {
        return ImmutableList.copyOf(accountabilities);
	}

    @Override
	public MeterActivation activate(Date start) {
		MeterActivationImpl result = meterActivationFactory.get().init(this, start);
		dataModel.persist(result);
		adopt(result);
		return result;
	}
    
    @Override
	public MeterActivation activate(Meter meter, Date start) {
		MeterActivationImpl result = meterActivationFactory.get().init(meter, this, start);
		dataModel.persist(result);
		adopt(result);
		return result;
	}
    
    public void adopt(MeterActivationImpl meterActivation) {
    	if (!meterActivations.isEmpty()) {
    		MeterActivationImpl last = meterActivations.get(meterActivations.size() - 1);
    		if (last.getStart().after(meterActivation.getStart())) {
    			throw new IllegalArgumentException("Invalid start date");
    		} else {
    			if (last.getEnd() == null || last.getEnd().after(meterActivation.getStart())) {
    				last.endAt(meterActivation.getStart());
    			}
    		}
    	}
    	Optional<Meter> meter = meterActivation.getMeter();
    	if (meter.isPresent()) {
    		((MeterImpl) meter.get()).adopt(meterActivation);
    	}
    	meterActivations.add(meterActivation);
    }
	
	@Override
	public UsagePointAccountability addAccountability(PartyRole role , Party party , Date start) {
		UsagePointAccountability accountability = accountabilityFactory.get().init(this, party, role, start);
		accountabilities.add(accountability);
		return accountability;
	}

    @Override
    public boolean hasAccountability(User user) {
        for (UsagePointAccountability accountability : getAccountabilities()) {
            for (PartyRepresentation representation : accountability.getParty().getCurrentDelegates()) {
                if (representation.getDelegate().equals(user)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Optional<UsagePointDetailImpl> getDetail(Date date) {
        return detail.effective(date);
    }

    @Override
    public List<UsagePointDetailImpl> getDetail(Interval interval) {
        return detail.effective(interval);
    }

    @Override
    public void addDetail(UsagePointDetail newDetail) {
        Optional<UsagePointDetailImpl> optional = this.getDetail(newDetail.getInterval().getStart());
        if (optional.isPresent()) {
            UsagePointDetailImpl previousDetail = optional.get();
            this.terminateDetail(previousDetail, newDetail.getInterval().getStart());
        }
        validateAddingDetail(newDetail);
        detail.add((UsagePointDetailImpl) newDetail);
        touch();
    }

    public void touch() {
        if (id != 0) {
            dataModel.touch(this);
        }
    }

    @Override
    public UsagePointDetail terminateDetail(UsagePointDetail detail, Date date) {
        UsagePointDetailImpl toUpdate = null;
        if (detail.getUsagePoint() == this) {
            toUpdate = (UsagePointDetailImpl) detail;
        }
        if (toUpdate == null || !detail.getInterval().isEffective(date)) {
            throw new IllegalArgumentException();
        }
        toUpdate.terminate(date);
        dataModel.update(toUpdate);
        touch();
        return toUpdate;
    }
    
    private void validateAddingDetail(UsagePointDetail candidate) {
        for (UsagePointDetail usagePointDetail : detail.effective(candidate.getInterval())) {
            if (candidate.conflictsWith(usagePointDetail)) {
                throw new IllegalArgumentException("Conflicts with existing usage point characteristics : " + candidate);
            }
        }
    }

    @Override
	public List<? extends BaseReadingRecord> getReadings(Interval interval, ReadingType readingType) {
		return MeterActivationsImpl.from(meterActivations, interval).getReadings(interval, readingType);
	}

	@Override
	public Set<ReadingType> getReadingTypes(Interval interval) {
		return MeterActivationsImpl.from(meterActivations, interval).getReadingTypes(interval);
	}

	@Override
	public List<? extends BaseReadingRecord> getReadingsBefore(Date when, ReadingType readingType, int count) {
		return MeterActivationsImpl.from(meterActivations).getReadingsBefore(when,readingType,count);
	}

	@Override
	public Optional<Party> getCustomer(Date when) {
		return getResponsibleParty(when,MarketRoleKind.ENERGYSERVICECONSUMER);
	}

	@Override
	public Optional<Party> getResponsibleParty(Date when, MarketRoleKind marketRole) {
		for (UsagePointAccountability each : getAccountabilities()) {
			if (each.isEffective(when) && each.getRole().getMRID().equals(marketRole.name())) {
				return Optional.of(each.getParty());
			}
		}
		return Optional.absent();
	}


}
