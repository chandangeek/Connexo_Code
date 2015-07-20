package com.elster.jupiter.metering;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.util.units.Quantity;

public interface ElectricityDetailBuilder {

	ElectricityDetailBuilder withAmiBillingReady(AmiBillingReadyKind amiBillingReady);

	ElectricityDetailBuilder withCheckBilling(Boolean checkBilling);

	ElectricityDetailBuilder withConnectionState(UsagePointConnectedKind connectionState);

	ElectricityDetailBuilder withMinimalUsageExpected(Boolean minimalUsageExpected);

	ElectricityDetailBuilder withServiceDeliveryRemark(String serviceDeliveryRemark);

	ElectricityDetailBuilder withGrounded(Boolean grounded);

	ElectricityDetailBuilder withNominalServiceVoltage(Quantity nominalServiceVoltage);

	ElectricityDetailBuilder withPhaseCode(PhaseCode phaseCode);

	ElectricityDetailBuilder withRatedCurrent(Quantity ratedCurrent);

	ElectricityDetailBuilder withRatedPower(Quantity ratedPower);

	ElectricityDetailBuilder withEstimatedLoad(Quantity estimatedLoad);
	
	AmiBillingReadyKind getAmiBillingReady();

	boolean isCheckBilling();

	UsagePointConnectedKind getConnectionState();

	boolean isMinimalUsageExpected();

	String getServiceDeliveryRemark();

	boolean isGrounded();

	Quantity getNominalServiceVoltage();

	PhaseCode getPhaseCode();

	Quantity getRatedCurrent();

	Quantity getRatedPower();

	Quantity getEstimatedLoad();

	ElectricityDetail build();

}
