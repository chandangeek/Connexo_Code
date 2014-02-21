package com.elster.jupiter.metering.rest.impl;

import java.util.Date;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.util.units.Quantity;
import com.google.common.base.Optional;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown=true)

public class UsagePointInfo {	
	private UsagePoint usagePoint;
	
	public long id;
	public ServiceKind serviceCategory;
    public long serviceLocationId;
    public String aliasName;
    public String description;
    public String mRID;
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
		isSdp = usagePoint.isSdp();
		isVirtual = usagePoint.isVirtual();
		outageRegion = usagePoint.getOutageRegion();
		readCycle = usagePoint.getReadCycle();
		readRoute = usagePoint.getReadRoute();
		servicePriority = usagePoint.getServicePriority();
		version = usagePoint.getVersion();
		createTime = usagePoint.getCreateDate().getTime();
		modTime = usagePoint.getModificationDate().getTime();
		Optional<? extends UsagePointDetail> detailHolder = usagePoint.getDetail(new Date());
		if (detailHolder.isPresent()) {
			UsagePointDetail detail = detailHolder.get();
			minimalUsageExpected = detail.isMinimalUsageExpected();
			amiBillingReady = detail.getAmiBillingReady();	
			checkBilling = detail.isCheckBilling();
			connectionState = detail.getConnectionState();
			serviceDeliveryRemark = detail.getServiceDeliveryRemark();
			if (detail instanceof ElectricityDetail) {
				ElectricityDetail eDetail = (ElectricityDetail) detail;
				estimatedLoad = eDetail.getEstimatedLoad();
				grounded = eDetail.isGrounded();
				nominalServiceVoltage = eDetail.getNominalServiceVoltage();
				phaseCode = eDetail.getPhaseCode();
				ratedCurrent = eDetail.getRatedCurrent();
				ratedPower = eDetail.getRatedPower();
			}
		}
		
	}
	
	void addServiceLocationInfo() {
		ServiceLocation location = usagePoint.getServiceLocation();
		if (location != null) {
			serviceLocation = new ServiceLocationInfo(location);
		}
	}
}
