package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.GasDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;

public class GasDetailBuilderImpl implements GasDetailBuilder{

	private AmiBillingReadyKind amiBillingReady;
	private boolean checkBilling;
	private UsagePointConnectedKind connectionState;
	private boolean minimalUsageExpected;
	private String serviceDeliveryRemark;

	private UsagePoint usagePoint;
	private Interval interval;
	private DataModel dataModel;

	
	public GasDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
		this.dataModel = dataModel;
		this.usagePoint = usagePointImpl;
		this.interval = interval;
	}


	@Override
	public GasDetailBuilder withAmiBillingReady(AmiBillingReadyKind amiBillingReady) {
		this.amiBillingReady = amiBillingReady;
		return this;
	}

	@Override
	public GasDetailBuilder withCheckBilling(Boolean checkBilling) {
		this.checkBilling = checkBilling;
		return this;
	}

	@Override
	public GasDetailBuilder withConnectionState(UsagePointConnectedKind connectionState) {
		this.connectionState = connectionState;
		return this;
	}

	@Override
	public GasDetailBuilder withMinimalUsageExpected(Boolean minimalUsageExpected) {
		this.minimalUsageExpected = minimalUsageExpected;
		return this;
	}
	
	@Override
	public GasDetailBuilder withServiceDeliveryRemark(String serviceDeliveryRemark) {
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
	public GasDetail build() {
		GasDetail gd =  dataModel.getInstance(GasDetailImpl.class).init(usagePoint, this, interval);
		usagePoint.addDetail(gd);
		return gd;
	}

}
