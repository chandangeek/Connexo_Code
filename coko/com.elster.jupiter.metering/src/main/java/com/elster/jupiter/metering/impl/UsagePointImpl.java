package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.EventType;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	private Instant createTime;
	private Instant modTime;
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
	
	public UsagePointBuilder getNewBuilder(ServiceCategory serviceCategory) {
		return new UsagePointBuilderImpl(serviceCategory, this);
	}
	
//	public UsagePointDetailBuilder getNewUsagePointDetailBuilder() {
//		if (id == 0) {
//			//cannot create a new usage point detail on an unsaved usagepoint
//			
//		}
//	}
    public UsagePointDetail newUsagePointDetail(Instant start) {
    	Interval interval = Interval.of(Range.atLeast(start));
    	ServiceKind kind = getServiceCategory().getKind();
        if (kind.equals(ServiceKind.ELECTRICITY)) {
            return ElectricityDetailImpl.from(dataModel, this, interval);
        } 
        else if (kind.equals(ServiceKind.GAS)) {
            return GasDetailImpl.from(dataModel, this, interval);
        } else if (kind.equals(ServiceKind.WATER)) {
            return WaterDetailImpl.from(dataModel, this, interval);
        } else {
            return DefaultDetailImpl.from(dataModel, this, interval);
        }
    }
    
    @Override
	public UsagePointDetailBuilder getNewUsagePointDetailBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ElectricityDetailBuilder newElectricityDetailBuilder(Instant start) {
		Interval interval = Interval.of(Range.atLeast(start));
		return new ElectricityDetailBuilderImpl(dataModel, this, interval);
	}
	
	
	
	UsagePointImpl init(UsagePointBuilder upb) {
		this.serviceCategory.set(upb.getServiceCategory());
		
		this.aliasName=upb.getAliasName();
		this.description=upb.getDescription();
		this.mRID = upb.getmRID();
		this.name=upb.getName();
		this.isSdp=upb.isSdp();
		this.isVirtual=upb.isVirtual();
		this.outageRegion=upb.getOutageRegion();
		this.readCycle=upb.getReadCycle();
		this.readRoute=upb.getReadRoute();
		this.servicePriority=upb.getServicePriority();
		
		save();
		return this;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override 
	public long getServiceLocationId() {
		return 0L;
//		ServiceLocation location = getServiceLocation().get();
//		return location == null ? 0 : location.getId();

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
	public Optional<ServiceLocation> getServiceLocation() {
		return serviceLocation.getOptional();
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
	public Optional<MeterActivation> getCurrentMeterActivation() {
        return meterActivations.stream()
                .filter(MeterActivation::isCurrent)
                .map(MeterActivation.class::cast)
                .findAny();
    }

	@Override
	public Instant getCreateDate() {
		return createTime;
	}

	@Override
	public Instant getModificationDate() {
		return modTime; 
	}

	public long getVersion() {
		return version;
	}

	@Override
	public List<UsagePointAccountability> getAccountabilities() {
        return ImmutableList.copyOf(accountabilities);
	}

    @Override
	public MeterActivation activate(Instant start) {
		MeterActivationImpl result = meterActivationFactory.get().init(this, start);
		dataModel.persist(result);
		adopt(result);
		return result;
	}

    @Override
	public MeterActivation activate(Meter meter, Instant start) {
		MeterActivationImpl result = meterActivationFactory.get().init(meter, this, start);
		dataModel.persist(result);
		adopt(result);
		return result;
	}

    public void adopt(MeterActivationImpl meterActivation) {
        meterActivations.stream()
                .filter(activation -> activation.getId() != meterActivation.getId())
                .reduce((m1, m2) -> m2)
                .ifPresent(last -> {
                    if (last.getRange().lowerEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
                        throw new IllegalArgumentException("Invalid start date");
                    } else {
                        if (!last.getRange().hasUpperBound()  || last.getRange().upperEndpoint().isAfter(meterActivation.getRange().lowerEndpoint())) {
                            last.endAt(meterActivation.getRange().lowerEndpoint());
                        }
                    }
                });
    	Optional<Meter> meter = meterActivation.getMeter();
    	if (meter.isPresent()) {
			// if meter happens to be the same meter of the last meteractivation that we just closed a few lines above,
			// best is to refresh the meter so the updated meteractivations are refetched from db. see COPL-854
			Meter existing = dataModel.mapper(Meter.class).getExisting(meter.get().getId());
			((MeterImpl) existing).adopt(meterActivation);
		}
    	meterActivations.add(meterActivation);
    }

	@Override
	public UsagePointAccountability addAccountability(PartyRole role , Party party , Instant start) {
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
    public Optional<UsagePointDetailImpl> getDetail(Instant instant) {
        return detail.effective(instant);
    }

    @Override
    public List<UsagePointDetailImpl> getDetail(Range<Instant> range) {
        return detail.effective(range);
    }

    @Override
    public void addDetail(UsagePointDetail newDetail) {
        Optional<UsagePointDetailImpl> optional = this.getDetail(newDetail.getRange().lowerEndpoint());
        if (optional.isPresent()) {
            UsagePointDetailImpl previousDetail = optional.get();
            this.terminateDetail(previousDetail, newDetail.getRange().lowerEndpoint());
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
    public UsagePointDetail terminateDetail(UsagePointDetail detail, Instant date) {
        UsagePointDetailImpl toUpdate = null;
        if (detail.getUsagePoint() == this) {
            toUpdate = (UsagePointDetailImpl) detail;
        }
        if (toUpdate == null || !detail.isEffectiveAt(date)) {
            throw new IllegalArgumentException();
        }
        toUpdate.terminate(date);
        dataModel.update(toUpdate);
        touch();
        return toUpdate;
    }

    private void validateAddingDetail(UsagePointDetail candidate) {
        for (UsagePointDetail usagePointDetail : detail.effective(candidate.getRange())) {
            if (candidate.conflictsWith(usagePointDetail)) {
                throw new IllegalArgumentException("Conflicts with existing usage point characteristics : " + candidate);
            }
        }
    }

    @Override
	public List<? extends BaseReadingRecord> getReadings(Range<Instant> range, ReadingType readingType) {
		return MeterActivationsImpl.from(meterActivations, range).getReadings(range, readingType);
	}

	@Override
	public List<? extends BaseReadingRecord> getReadingsUpdatedSince(Range<Instant> range, ReadingType readingType, Instant since) {
		return MeterActivationsImpl.from(meterActivations, range).getReadingsUpdatedSince(range, readingType, since);
	}

	@Override
	public Set<ReadingType> getReadingTypes(Range<Instant> range) {
		return MeterActivationsImpl.from(meterActivations, range).getReadingTypes(range);
	}

	@Override
	public List<? extends BaseReadingRecord> getReadingsBefore(Instant when, ReadingType readingType, int count) {
		return MeterActivationsImpl.from(meterActivations).getReadingsBefore(when, readingType, count);
	}

	@Override
	public List<? extends BaseReadingRecord> getReadingsOnOrBefore(Instant when, ReadingType readingType, int count) {
		return MeterActivationsImpl.from(meterActivations).getReadingsOnOrBefore(when, readingType, count);
	}

    @Override
    public boolean hasData() {
        return MeterActivationsImpl.from(meterActivations).hasData();
    }

	@Override
	public Optional<Party> getCustomer(Instant when) {
		return getResponsibleParty(when, MarketRoleKind.ENERGYSERVICECONSUMER);
	}

	@Override
	public Optional<Party> getResponsibleParty(Instant when, MarketRoleKind marketRole) {
		for (UsagePointAccountability each : getAccountabilities()) {
			if (each.isEffectiveAt(when) && each.getRole().getMRID().equals(marketRole.name())) {
				return Optional.of(each.getParty());
			}
		}
		return Optional.empty();
	}

    @Override
    public boolean is(ReadingContainer other) {
        return other instanceof UsagePoint && ((UsagePoint) other).getId() == getId();
    }

    @Override
    public Optional<Meter> getMeter(Instant instant) {
        return getMeterActivation(instant).flatMap(MeterActivation::getMeter);
    }

    @Override
    public Optional<? extends MeterActivation> getMeterActivation(Instant when) {
        return meterActivations.stream()
                .filter(meterActivation -> meterActivation.isEffectiveAt(when))
                .findFirst();
    }

    @Override
    public Optional<UsagePoint> getUsagePoint(Instant instant) {
        return Optional.of(this);
    }

	@Override
	public ZoneId getZoneId() {
		return getCurrentMeterActivation()
				.map(MeterActivation::getZoneId)
				.orElse(ZoneId.systemDefault());
	}

}
