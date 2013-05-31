package com.elster.jupiter.metering.rest;

import javax.xml.bind.annotation.XmlRootElement;

import com.elster.jupiter.metering.*;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.cbo.PhaseCode;

@XmlRootElement
public class UsagePointInfo {	
	private UsagePoint usagePoint;
	
	public long id;
	public String mRID;
	public ServiceKind serviceCategory;
	public long serviceLocationId;
	public String aliasName;
	public String description;
	public String name;
	public AmiBillingReadyKind amiBillingReady;
	public boolean checkBilling;
	public UsagePointConnectedKind connectionState;
	public Quantity estimatedLoad;
	public boolean grounded;
	public boolean isSdp;
	public boolean isVirtual;
	public boolean minimalUsageExpected;
	public Quantity nominalServiceVoltage;
	public String outageRegion;
	public PhaseCode phaseCode;
	public Quantity ratedCurrent;
	public Quantity ratedPower;
	public String readCycle;
	public String readRoute;
	public String serviceDeliveryRemark;
	public String servicePriority;
	public long version;
	public long createTime;
	public long modTime;
	public ServiceLocationInfo serviceLocation;
	
	public UsagePointInfo() {
	}
	
	public UsagePointInfo(UsagePoint usagePoint) {
		this.usagePoint = usagePoint;		
		id = usagePoint.getId();
		mRID = usagePoint.getMRID();
		serviceCategory = usagePoint.getServiceCategory().getKind();
		serviceLocationId = usagePoint.getServiceLocationId();
		aliasName = usagePoint.getAliasName();
		description = usagePoint.getDescription();
		name = usagePoint.getName();
		amiBillingReady = usagePoint.getAmiBillingReady();
		checkBilling = usagePoint.isCheckBilling();
		connectionState = usagePoint.getConnectionState();
		estimatedLoad = usagePoint.getEstimatedLoad();
		grounded = usagePoint.isGrounded();
		isSdp = usagePoint.isSdp();
		isVirtual = usagePoint.isVirtual();
		minimalUsageExpected = usagePoint.isMinimumUsageExpected();
		nominalServiceVoltage = usagePoint.getNominalServiceVoltage();
		outageRegion = usagePoint.getOutageRegion();
		phaseCode = usagePoint.getPhaseCode();
		ratedCurrent = usagePoint.getRatedCurrent();
		ratedPower = usagePoint.getRatedPower();
		readCycle = usagePoint.getReadCycle();
		readRoute = usagePoint.getReadRoute();
		serviceDeliveryRemark = usagePoint.getServiceDeliveryRemark();
		servicePriority = usagePoint.getServicePriority();
		version = usagePoint.getVersion();
		createTime = usagePoint.getCreateDate().getTime();
		modTime = usagePoint.getModificationDate().getTime();

	}
	
	void addServiceLocationInfo() {
		ServiceLocation location = usagePoint.getServiceLocation();
		if (location != null) {
			serviceLocation = new ServiceLocationInfo(location);
		}
	}
}
