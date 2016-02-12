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

    void setName(String type);

    String getVersionName();

    void setVersionName(String versionName);

    Status getStatus();

    void setStatus(Status status);

    LogLevel getLogLevel();

    void setLogLevel(LogLevel logLevel);

    Optional<ServiceCallLifeCycle> getServiceCallLifeCycle();

    Optional<CustomPropertySet<ServiceCall, ? extends PersistentDomainExtension<ServiceCall>>> getCustomPropertySet();

    void save();
}
