package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.util.List;
import java.util.Optional;

/**
 * Created by bvn on 2/8/16.
 */
public interface ServiceCallType extends HasId, HasName {
    long getVersion();

    String getVersionName();

    Status getStatus();

    void deprecate();

    LogLevel getLogLevel();

    void setLogLevel(LogLevel logLevel);

    Optional<ServiceCallLifeCycle> getServiceCallLifeCycle();

    Optional<DefaultState> getCurrentLifeCycleState();

    void setCurrentLifeCycleState(DefaultState currentLifeCycleState);

    /**
     * Returns the RegisteredCustomPropertySets linked to this ServiceCallType
     * @return
     */
    List<RegisteredCustomPropertySet> getCustomPropertySets();

    void addCustomPropertySet(RegisteredCustomPropertySet customPropertySet);

    void removeCustomPropertySet(RegisteredCustomPropertySet customPropertySet);

    void save();
}
