/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface ServiceCallType extends HasId, HasName {
    long getVersion();

    String getVersionName();

    ServiceCallHandler getServiceCallHandler();

    Status getStatus();

    void deprecate();

    LogLevel getLogLevel();

    void setLogLevel(LogLevel logLevel);

    ServiceCallLifeCycle getServiceCallLifeCycle();

    /**
     * Returns the RegisteredCustomPropertySets linked to this ServiceCallType
     * @return
     */
    List<RegisteredCustomPropertySet> getCustomPropertySets();

    void addCustomPropertySet(RegisteredCustomPropertySet customPropertySet);

    void removeCustomPropertySet(RegisteredCustomPropertySet customPropertySet);

    ServiceCallBuilder newServiceCall();

    void save();

    void delete();
}
