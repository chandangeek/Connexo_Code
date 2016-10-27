package com.elster.jupiter.mdm.usagepoint.lifecycle;

import com.elster.jupiter.fsm.StandardStateTransitionEventType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

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

        public static Optional<Level> forPrivilege(String privilege) {
            for (Level level : values()) {
                if (level.getPrivilege().equals(privilege)) {
                    return Optional.of(level);
                }
            }
            return Optional.empty();
        }
    }

    UsagePointState getFrom();

    UsagePointState getTo();

    Optional<StandardStateTransitionEventType> getTriggeredBy();

    UsagePointLifeCycle getLifeCycle();

    Set<MicroAction> getActions();

    Set<MicroCheck> getChecks();

    Set<Level> getLevels();

    long getVersion();

    interface UsagePointTransitionCreator {

        UsagePointTransitionCreator triggeredBy(StandardStateTransitionEventType eventType);

        UsagePointTransitionCreator withActions(Set<MicroAction.Key> microActionKeys);

        UsagePointTransitionCreator withChecks(Set<MicroCheck.Key> microCheckKeys);

        UsagePointTransitionCreator withLevels(Set<Level> levels);

        UsagePointTransition complete();
    }
}
