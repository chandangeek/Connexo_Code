package com.elster.jupiter.servicecall;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * ServiceCall is the tracker for external actions into connexo. It acts as logger, state-holder and also drives
 * the ServiceCallHandler(if any) through the state changes.
 *
 * Created by bvn on 2/4/16.
 */
public interface ServiceCall extends HasId, HasName {
    Instant getCreationDate();

    Instant getLastModificationDate();

    Optional<Instant> getLastCompletedDate();

    DefaultState getState();

    void setState(DefaultState state);

    Optional<String> getOrigin();

    Optional<String> getExternalReference();

    Optional<List<RegisteredCustomPropertySet>> getCustomProperties();

    Optional<RefAny> getTargetObject();

    Optional<ServiceCall> getParent();

    ServiceCallType getType();

    void cancel();

    void save();
}
