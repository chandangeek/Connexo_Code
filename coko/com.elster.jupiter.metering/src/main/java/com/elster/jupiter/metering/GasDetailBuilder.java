package com.elster.jupiter.metering;

public interface GasDetailBuilder {

	GasDetailBuilder withAmiBillingReady(AmiBillingReadyKind amiBillingReady);

	GasDetailBuilder withCheckBilling(Boolean checkBilling);

	GasDetailBuilder withConnectionState(UsagePointConnectedKind connectionState);

	GasDetailBuilder withMinimalUsageExpected(Boolean minimalUsageExpected);

	GasDetailBuilder withServiceDeliveryRemark(String serviceDeliveryRemark);

	AmiBillingReadyKind getAmiBillingReady();

	boolean isCheckBilling();

	UsagePointConnectedKind getConnectionState();

	boolean isMinimalUsageExpected();

	String getServiceDeliveryRemark();

	GasDetail build();

}
