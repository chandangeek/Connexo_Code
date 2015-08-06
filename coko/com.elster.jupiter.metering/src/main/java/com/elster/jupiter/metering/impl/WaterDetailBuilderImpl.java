package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.WaterDetailBuilder;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;

public class WaterDetailBuilderImpl implements WaterDetailBuilder{

	private AmiBillingReadyKind amiBillingReady;
	private boolean checkBilling;
	private UsagePointConnectedKind connectionState;
	private boolean minimalUsageExpected;
	private String serviceDeliveryRemark;

	private UsagePoint usagePoint;
	private Interval interval;
	private DataModel dataModel;

	
	public WaterDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
		this.dataModel = dataModel;
		this.usagePoint = usagePointImpl;
		this.interval = interval;
	}


	@Override
	public WaterDetailBuilder withAmiBillingReady(AmiBillingReadyKind amiBillingReady) {
		this.amiBillingReady = amiBillingReady;
		return this;
	}

	@Override
	public WaterDetailBuilder withCheckBilling(Boolean checkBilling) {
		this.checkBilling = checkBilling;
		return this;
	}

	@Override
	public WaterDetailBuilder withConnectionState(UsagePointConnectedKind connectionState) {
		this.connectionState = connectionState;
		return this;
	}

	@Override
	public WaterDetailBuilder withMinimalUsageExpected(Boolean minimalUsageExpected) {
		this.minimalUsageExpected = minimalUsageExpected;
		return this;
	}
	
	@Override
	public WaterDetailBuilder withServiceDeliveryRemark(String serviceDeliveryRemark) {
		this.serviceDeliveryRemark=serviceDeliveryRemark;
		return this;
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
	public boolean isMinimalUsageExpected() {
		return minimalUsageExpected;
	}

	@Override
	public String getServiceDeliveryRemark() {
		return serviceDeliveryRemark;
	}

	@Override
	public WaterDetail build() {
		WaterDetail wd =  dataModel.getInstance(WaterDetailImpl.class).init(usagePoint, this, interval);
		usagePoint.addDetail(wd);
        return wd;
    }

}
