package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.units.Quantity;
import com.google.common.base.Optional;

import java.util.Date;
import java.util.List;

public interface UsagePoint extends HasName {
	long getId();
	String getAliasName();
	String getDescription();
	String getMRID();
	AmiBillingReadyKind getAmiBillingReady();
	boolean isCheckBilling();
	UsagePointConnectedKind getConnectionState();
	Quantity getEstimatedLoad();
	boolean isGrounded();
	boolean isSdp();
	boolean isVirtual();
	boolean isMinimumUsageExpected();
	Quantity getNominalServiceVoltage();
	String getOutageRegion();
	PhaseCode getPhaseCode();
	Quantity getRatedCurrent();
	Quantity getRatedPower();
	String getReadCycle();
	String getReadRoute();
	String getServiceDeliveryRemark();
	String getServicePriority();
    List<MeterActivation> getMeterActivations();
	MeterActivation getCurrentMeterActivation();

	long getServiceLocationId();
	ServiceLocation getServiceLocation();
	ServiceCategory getServiceCategory();

	void setServiceLocation(ServiceLocation serviceLocation);
	void setServicePriority(String servicePriority);
	void setServiceDeliveryRemark(String serviceDeliveryRemark);
	void setReadRoute(String readRoute);
	void setReadCycle(String readCycle);
	void setOutageRegion(String outageRegion);
	void setVirtual(boolean isVirtual);
	void setSdp(boolean isSdp);
	void setGrounded(boolean grounded);
	void setName(String name);
	void setMRID(String mRID);
	void setDescription(String description);
	void setAliasName(String aliasName);
	void setPhaseCode(PhaseCode phaseCode);
	void setMinimalUsageExpected(boolean minimalUsageExpected);
	void setConnectionState(UsagePointConnectedKind kind);
	void setCheckBilling(boolean checkBilling);
	void setAmiBillingReady(AmiBillingReadyKind kind);

	void setEstimatedLoad(Quantity estimatedLoad);
	void setNominalServiceVoltage(Quantity nominalServiceVoltage);
	void setRatedCurrent(Quantity ratedCurrent);
	void setRatedPower(Quantity ratedPower);

	void save();

	Date getCreateDate();
	Date getModificationDate();
	long getVersion();

	MeterActivation activate(Date start);
    List<UsagePointAccountability> getAccountabilities();
	UsagePointAccountability addAccountability(PartyRole role, Party party, Date start);
	Optional<Party> getResponsibleParty(PartyRole role);

	boolean hasAccountability(User user);

    void delete();
}
