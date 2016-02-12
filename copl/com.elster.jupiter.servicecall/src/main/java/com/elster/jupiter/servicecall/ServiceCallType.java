package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

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

    Optional<CustomPropertySet<ServiceCall, ? extends PersistentDomainExtension<ServiceCall>>> getCustomPropertySet();

    void save();
}
