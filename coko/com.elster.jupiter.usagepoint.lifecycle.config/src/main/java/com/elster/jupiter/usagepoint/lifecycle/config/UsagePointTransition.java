/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.config;

import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@ProviderType
public interface UsagePointTransition extends HasId, HasName {
    enum Level {
        ONE(Privileges.Constants.EXECUTE_TRANSITION_1),
        TWO(Privileges.Constants.EXECUTE_TRANSITION_2),
        THREE(Privileges.Constants.EXECUTE_TRANSITION_3),
        FOUR(Privileges.Constants.EXECUTE_TRANSITION_4);

        public String getPrivilege() {
            return privilege;
        }

        private String privilege;

        Level(String privilege) {
            this.privilege = privilege;
        }
    }

    UsagePointState getFrom();

    UsagePointState getTo();

    Optional<StandardStateTransitionEventType> getTriggeredBy();

    UsagePointLifeCycle getLifeCycle();

    Set<MicroAction> getActions();

    Set<MicroCheck> getChecks();

    Set<Level> getLevels();

    void remove();

    UsagePointTransitionUpdater startUpdate();

    void doTransition(String sourceId, String sourceType, Instant transitionTime, Map<String, Object> properties);

    long getVersion();

    List<PropertySpec> getMicroActionsProperties();

    @ProviderType
    interface UsagePointTransitionCreator<T extends UsagePointTransitionCreator> {

        T triggeredBy(StandardStateTransitionEventType eventType);

        T withActions(Set<String> microActionKeys);

        T withChecks(Set<String> microCheckKeys);

        T withLevels(Set<Level> levels);

        UsagePointTransition complete();
    }

    @ProviderType
    interface UsagePointTransitionUpdater extends UsagePointTransitionCreator<UsagePointTransitionUpdater> {

        UsagePointTransitionUpdater withName(String name);

        UsagePointTransitionUpdater from(UsagePointState state);

        UsagePointTransitionUpdater to(UsagePointState state);
    }
}
