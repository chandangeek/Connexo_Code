package com.elster.jupiter.metering;

import com.elster.jupiter.cps.CustomPropertySet;

import java.util.List;

public interface ServiceCategoryCustomPropertySet {

    List<ServiceKind> getServiceKinds();

    CustomPropertySet getCustomPropertySet();
}
