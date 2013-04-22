package com.elster.jupiter.metering;

import java.util.Date;
import java.util.List;

import com.elster.jupiter.cbo.*;
import com.elster.jupiter.units.Quantity;

public interface UsagePoint {
	long getId();
	String getAliasName();
	String getDescription();
	String getMRID();
	String getName();
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
	List<MeterActivation> getMeterActivations();
		
}
