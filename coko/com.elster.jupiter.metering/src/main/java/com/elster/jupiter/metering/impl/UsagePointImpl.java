package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
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
import com.elster.jupiter.util.units.Quantity;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import javax.inject.Provider;
import javax.inject.Inject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
	private final List<MeterActivation> meterActivations = new ArrayList<>();
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
	public List<MeterActivation> getMeterActivations() {
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
		return result;
	}
	
	@Override
	public UsagePointAccountability addAccountability(PartyRole role , Party party , Date start) {
		UsagePointAccountability accountability = accountabilityFactory.get().init(this, party, role, start);
		accountabilities.add(accountability);
		return accountability;
	}
	
	@Override
	public Optional<Party> getResponsibleParty(PartyRole role) {
		for (UsagePointAccountability each : getAccountabilities()) {
			if (each.isCurrent() && each.getRole().equals(role)) {
				return Optional.of(each.getParty());
			}
		}
		return Optional.absent();
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






}
