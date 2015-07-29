package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;

public class ElectricityDetailBuilderImpl implements ElectricityDetailBuilder{

	private AmiBillingReadyKind amiBillingReady;
	private boolean checkBilling;
	private UsagePointConnectedKind connectionState;
	private boolean minimalUsageExpected;
	private String serviceDeliveryRemark;

	private boolean grounded;
	private Quantity nominalServiceVoltage;
	private PhaseCode phaseCode;
	private Quantity ratedCurrent;
	private Quantity ratedPower;
	private Quantity estimatedLoad;
	
	private UsagePoint usagePoint;
	private Interval interval;
	private DataModel dataModel;

	
	public ElectricityDetailBuilderImpl(DataModel dataModel, UsagePointImpl usagePointImpl, Interval interval) {
		this.dataModel = dataModel;
		this.usagePoint = usagePointImpl;
		this.interval = interval;
	}


	@Override
	public ElectricityDetailBuilder withAmiBillingReady(AmiBillingReadyKind amiBillingReady) {
		this.amiBillingReady = amiBillingReady;
		return this;
	}

	@Override
	public ElectricityDetailBuilder withCheckBilling(Boolean checkBilling) {
		this.checkBilling = checkBilling;
		return this;
	}

	@Override
	public ElectricityDetailBuilder withConnectionState(UsagePointConnectedKind connectionState) {
		this.connectionState = connectionState;
		return this;
	}

	@Override
	public ElectricityDetailBuilder withMinimalUsageExpected(Boolean minimalUsageExpected) {
		this.minimalUsageExpected = minimalUsageExpected;
		return this;
	}
	
	@Override
	public ElectricityDetailBuilder withServiceDeliveryRemark(String serviceDeliveryRemark) {
		this.serviceDeliveryRemark=serviceDeliveryRemark;
		return this;
	}

	@Override
	public ElectricityDetailBuilder withGrounded(Boolean grounded) {
		this.grounded = grounded;
		return this;
	}
	
	@Override
	public ElectricityDetailBuilder withNominalServiceVoltage(Quantity nominalServiceVoltage) {
		this.nominalServiceVoltage = nominalServiceVoltage;
		return this;
	}
	
	@Override
	public ElectricityDetailBuilder withPhaseCode(PhaseCode phaseCode) {
		this.phaseCode = phaseCode;
		return this;
	}
	
	@Override
	public ElectricityDetailBuilder withRatedCurrent(Quantity ratedCurrent) {
		this.ratedCurrent = ratedCurrent;
		return this;
	}
	
	@Override
	public ElectricityDetailBuilder withRatedPower(Quantity ratedPower) {
		this.ratedPower = ratedPower;
		return this;
	}
	
	@Override
	public ElectricityDetailBuilder withEstimatedLoad(Quantity estimatedLoad) {
		this.estimatedLoad = estimatedLoad;
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
	public boolean isGrounded() {
		return grounded;
	}

	@Override
	public Quantity getNominalServiceVoltage() {
		return nominalServiceVoltage;
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
	public Quantity getEstimatedLoad() {
		return estimatedLoad;
	}

	@Override
	public ElectricityDetail build() {
		ElectricityDetail ed =  dataModel.getInstance(ElectricityDetailImpl.class).init(usagePoint, this, interval);
		usagePoint.addDetail(ed);
		return ed;
	}

}
