package com.elster.jupiter.metering;

public interface WaterDetailBuilder {

	WaterDetailBuilder withAmiBillingReady(AmiBillingReadyKind amiBillingReady);

	WaterDetailBuilder withCheckBilling(Boolean checkBilling);

	WaterDetailBuilder withConnectionState(UsagePointConnectedKind connectionState);

	WaterDetailBuilder withMinimalUsageExpected(Boolean minimalUsageExpected);

	WaterDetailBuilder withServiceDeliveryRemark(String serviceDeliveryRemark);

	AmiBillingReadyKind getAmiBillingReady();

	boolean isCheckBilling();

	UsagePointConnectedKind getConnectionState();

	boolean isMinimalUsageExpected();

	String getServiceDeliveryRemark();

	WaterDetail build();

}
