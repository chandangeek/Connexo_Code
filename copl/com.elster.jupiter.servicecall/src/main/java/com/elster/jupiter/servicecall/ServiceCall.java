/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.orm.TransactionRequired;
import com.elster.jupiter.util.HasId;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.Optional;

/**
 * ServiceCall is the tracker for external actions into connexo. It acts as logger, state-holder and also drives
 * the ServiceCallHandler(if any) through the state changes.
 *
 * Created by bvn on 2/4/16.
 */
@ProviderType
public interface ServiceCall extends HasId {

    String getNumber();

    Instant getCreationTime();

    Instant getLastModificationTime();

    Optional<Instant> getLastCompletedTime();

    DefaultState getState();

    @TransactionRequired
    void requestTransition(DefaultState state);

    Optional<String> getOrigin();

    Optional<String> getExternalReference();

    Optional<?> getTargetObject();

    Optional<ServiceCall> getParent();

    ServiceCallType getType();

    long getVersion();

    ServiceCallBuilder newChildCall(ServiceCallType serviceCallType);

    /**
     * Returns a chronologically sorted list of log entries for this service call
     */
    Finder<ServiceCallLog> getLogs();

    void log(LogLevel logLevel, String message);

    void cancel();

    void log(String message, Exception exception);

    void save();

    Finder<ServiceCall> findChildren(ServiceCallFilter filter);

    Finder<ServiceCall> findChildren();

    <T extends PersistentDomainExtension<ServiceCall>> Optional<T> getExtensionFor(CustomPropertySet<ServiceCall, T> customPropertySet, Object... additionalPrimaryKeyValues);

    <T extends PersistentDomainExtension<ServiceCall>> CustomPropertySetValues getValuesFor(CustomPropertySet<ServiceCall, T> customPropertySet, Object... additionalPrimaryKeyValues);

    <T extends PersistentDomainExtension<ServiceCall>> Optional<T> getExtension(Class<T> extensionClass, Object... additionalPrimaryKeyValues);

    void update(PersistentDomainExtension<ServiceCall> extension, Object... additionalPrimaryKeyValues);

    /**
     * Service call will be deleted from the system, together with all logs, children en logs on children.
     */
    void delete();

    boolean canTransitionTo(DefaultState targetState);
}
