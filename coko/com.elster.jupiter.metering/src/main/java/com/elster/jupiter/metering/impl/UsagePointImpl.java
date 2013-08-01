package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.orm.PersistenceEvent;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.util.units.Quantity;

import java.util.Date;
import java.util.List;

public class UsagePointImpl implements UsagePoint {
	// persistent fields
	private long id;
	private ServiceKind serviceKind;
	private long serviceLocationId;
	private String aliasName;
	private String description;
	private String mRID;
	private String name;
	private AmiBillingReadyKind amiBillingReady;
	private boolean checkBilling;
	private UsagePointConnectedKind connectionState;
	private Quantity estimatedLoad;
	private boolean grounded;
	private boolean isSdp;
	private boolean isVirtual;
	private boolean minimalUsageExpected;
	private Quantity nominalServiceVoltage;
	private String outageRegion;
	private PhaseCode phaseCode;
	private Quantity ratedCurrent;	
	private Quantity ratedPower;
	private String readCycle;
	private String readRoute;
	private String serviceDeliveryRemark;
	private String servicePriority;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime;
	@SuppressWarnings("unused")
	private String userName;
	private MeterActivation currentMeterActivation;
	
    // associations
	private ServiceCategory serviceCategory;
	private ServiceLocation serviceLocation;
	private List<MeterActivation> meterActivations;
	private List<UsagePointAccountability> accountabilities;
	
	@SuppressWarnings("unused")
	private UsagePointImpl() {
	}
	
	UsagePointImpl(String mRID , ServiceCategory serviceCategory) {
		this.mRID = mRID;
		this.serviceKind = serviceCategory.getKind();
		this.serviceCategory = serviceCategory;
		this.isSdp = true;
		
	}
	
	@Override
	public long getId() {
		return id;
	}

	@Override 
	public long getServiceLocationId() {
		return id;
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
	public AmiBillingReadyKind getAmiBillingReady() {
		return amiBillingReady;
	}

	@Override
	public boolean isCheckBilling() {
		return checkBilling;
	}

	@Override
	public UsagePointConnectedKind getConnectionState() {
		return connectionState;
	}

	@Override
	public Quantity getEstimatedLoad() {
		return estimatedLoad;
	}

	@Override
	public boolean isGrounded() {
		return grounded;
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
	public boolean isMinimumUsageExpected() {
		return minimalUsageExpected;
	}

	@Override
	public Quantity getNominalServiceVoltage() {
		return nominalServiceVoltage;
	}

	@Override
	public String getOutageRegion() {
		return outageRegion;
	}

	@Override
	public PhaseCode getPhaseCode() {
		return phaseCode;
	}

	@Override
	public Quantity getRatedCurrent() {
		return ratedCurrent;
	}

	@Override
	public Quantity getRatedPower() {
		return ratedPower;
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
	public String getServiceDeliveryRemark() {
		return serviceDeliveryRemark;
	}

	@Override
	public String getServicePriority() {
		return servicePriority;
	}

	@Override
	public ServiceCategory getServiceCategory() {
		if (serviceCategory == null) {
			serviceCategory = Bus.getOrmClient().getServiceCategoryFactory().getExisting(serviceKind);
		}
		return serviceCategory;
	}

	@Override
	public ServiceLocation getServiceLocation() {
		if (serviceLocationId == 0) {
			return null;
		}
		if (serviceLocation == null ) {
			serviceLocation = Bus.getOrmClient().getServiceLocationFactory().getExisting(serviceLocationId);
		}
		return serviceLocation;
	}

	@Override
	public void setAmiBillingReady(AmiBillingReadyKind kind) {
		this.amiBillingReady = kind;
	}

	@Override
	public void setCheckBilling(boolean checkBilling) {
		this.checkBilling = checkBilling;
	}

	@Override
	public void setConnectionState(UsagePointConnectedKind kind) {
		this.connectionState = kind;
	}

	@Override
	public void setMinimalUsageExpected(boolean minimalUsageExpected) {
		this.minimalUsageExpected = minimalUsageExpected;
	}

	@Override
	public void setPhaseCode(PhaseCode phaseCode) {
		this.phaseCode = phaseCode;
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
	public void setGrounded(boolean grounded) {
		this.grounded = grounded;
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
	public void setServiceDeliveryRemark(String serviceDeliveryRemark) {
		this.serviceDeliveryRemark = serviceDeliveryRemark;
	}

	@Override
	public void setServicePriority(String servicePriority) {
		this.servicePriority = servicePriority;
	}

	@Override
	public void setServiceLocation(ServiceLocation serviceLocation) {
		this.serviceLocation = serviceLocation;
		this.serviceLocationId = serviceLocation == null ? 0 : serviceLocation.getId();
	}

	@Override
	public void save() {
		if (id == 0) {
			Bus.getOrmClient().getUsagePointFactory().persist(this);
            Bus.getPublisher().publish(this, PersistenceEvent.CREATED);
		} else { 
			Bus.getOrmClient().getUsagePointFactory().update(this);
            Bus.getPublisher().publish(this, PersistenceEvent.UPDATED);
		}
	}

	@Override
	public void setEstimatedLoad(Quantity estimatedLoad) {
		this.estimatedLoad = estimatedLoad;		
	}

	@Override
	public void setNominalServiceVoltage(Quantity nominalServiceVoltage) {
		this.nominalServiceVoltage = nominalServiceVoltage;
	}

	@Override
	public void setRatedCurrent(Quantity ratedCurrent) {
		this.ratedCurrent = ratedCurrent;			
	}

	@Override
	public void setRatedPower(Quantity ratedPower) {
		this.ratedPower = ratedPower;		
	}


	@Override
	public List<MeterActivation> getMeterActivations() {
		return getMeterActivations(true);
	}
	
	private  List<MeterActivation> getMeterActivations(boolean protect) {
		if (meterActivations == null) {
			meterActivations = Bus.getOrmClient().getMeterActivationFactory().find("usagePoint",this);
		}
		return meterActivations;
	}
	
	@Override
	public MeterActivation getCurrentMeterActivation() {
		if (currentMeterActivation != null) {
			if (currentMeterActivation.isCurrent()) {
				return currentMeterActivation;
			} else {
				currentMeterActivation = null;
			}
		} else {
			for (MeterActivation each : getMeterActivations(false)) {
				if (each.isCurrent()) {
					return each;
				}
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
		if (accountabilities == null) {
			accountabilities = Bus.getOrmClient().getUsagePointAccountabilityFactory().find("usagePoint",this);
		}
		return accountabilities;
	}

	@Override
	public MeterActivation activate(Date start) {
		MeterActivation result = new MeterActivationImpl(this, start);
		Bus.getOrmClient().getMeterActivationFactory().persist(result);
		return result;
	}
	
	@Override
	public UsagePointAccountability addAccountability(PartyRole role , Party party , Date start) {
		UsagePointAccountability accountability = new UsagePointAccountabilityImpl(this,party,role,start);		
		this.accountabilities.add(accountability);
		Bus.getOrmClient().getUsagePointAccountabilityFactory().persist(accountability);
		return accountability;
	}
	
	@Override
	public Party getResponsibleParty(PartyRole role) {
		for (UsagePointAccountability each : getAccountabilities()) {
			if (each.isCurrent() && each.getRole().equals(role)) {
				return each.getParty();
			}
		}
		return null;
	}

    @Override
    public boolean hasAccountability(User user) {
        for (UsagePointAccountability accountability : getAccountabilities()) {
            for (User delegate : accountability.getParty().getCurrentDelegates()) {
                if (delegate.equals(user)) {
                    return true;
                }
            }
        }
        return false;
    }

}
