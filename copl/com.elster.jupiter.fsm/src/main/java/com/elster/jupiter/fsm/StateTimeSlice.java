package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.users.User;
import com.google.common.collect.Range;

import java.time.Instant;
import java.util.Optional;

/**
 * Models a slice of the {@link StateTimeline}
 * and specifies the {@link State} of an Object
 * during a period in time.
 * The period in time is of type "closed-open"
 * or to use the conventions of the Range class:
 * {@code [a..b)} or in mathematical writing {@code {x | a <= x < b}}
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-07 (08:36)
 */
@ProviderType
public interface StateTimeSlice {

    public Range<Instant> getPeriod();

    public State getState();

    public Optional<User> getUser();

}