package com.elster.jupiter.metering;

import com.elster.jupiter.orm.associations.Effectivity;

/**
 * Created with IntelliJ IDEA.
 * User: igh
 * Date: 19/02/14
 * Time: 9:12
 * To change this template use File | Settings | File Templates.
 */
public interface UsagePointDetail extends Effectivity {

    AmiBillingReadyKind getAmiBillingReady();

    boolean isCheckBilling();

    UsagePointConnectedKind getConnectionState();

    boolean isMinimalUsageExpected();

    String getServiceDeliveryRemark();

    void setAmiBillingReady(AmiBillingReadyKind amiBillingReady);

    void setCheckBilling(boolean checkBilling);

    void setConnectionState(UsagePointConnectedKind connectionState);

    void setMinimalUsageExpected(boolean minimalUsageExpected);

    void setServiceDeliveryRemark(String serviceDeliveryRemark);

    boolean isCurrent();

    boolean conflictsWith(UsagePointDetail other);

    UsagePoint getUsagePoint();

    void update();
}
