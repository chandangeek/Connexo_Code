package com.elster.jupiter.metering;

public interface UsagePointDetailBuilder {

    UsagePointDetailBuilder withAmiBillingReady(AmiBillingReadyKind amiBillingReady);

    UsagePointDetailBuilder withCheckBilling(boolean checkBilling);

    UsagePointDetailBuilder withConnectionState(UsagePointConnectedKind connectionState);

    UsagePointDetailBuilder withMinimalUsageExpected(boolean minimalUsageExpected);

    UsagePointDetailBuilder withServiceDeliveryRemark(String serviceDeliveryRemark);

}
