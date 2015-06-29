package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.events.EventType;

/**
 * Enables a standard {@link EventType} as a {@link StateTransitionEventType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-05 (10:02)
 */
@ProviderType
public interface StandardStateTransitionEventType extends StateTransitionEventType {

    public EventType getEventType();

}