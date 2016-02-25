package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
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

    void requestTransition(DefaultState state);

    Optional<String> getOrigin();

    Optional<String> getExternalReference();

    Optional<?> getTargetObject();

    Optional<ServiceCall> getParent();

    ServiceCallType getType();

    ServiceCallBuilder newChildCall(ServiceCallType serviceCallType);

    void cancel();

    void save();

    <T extends PersistentDomainExtension<ServiceCall>> Optional<T> getExtensionFor(CustomPropertySet<ServiceCall, T> customPropertySet, Object... additionalPrimaryKeyValues);

    <T extends PersistentDomainExtension<ServiceCall>> Optional<T> getExtension(Class<T> extensionClass, Object... additionalPrimaryKeyValues);
}
