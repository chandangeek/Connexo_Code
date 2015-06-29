package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

/**
 * Models timeline of the state changes of a statefull object.
 * A StateTimeline is effectively a collection of {@link StateTimeSlice slices}.
 * The timeline should not have any gaps because an object is
 * always in some state until it is being deleted.
 * Therefore, each slice abuts with the previous.
 * In other words:
 * <code>[t1, t2), [t2, t3), ... [tn-1, tn)</code>
 * is an example of the timeline where the end
 * of each slice is the start of the next.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-07 (08:31)
 */
@ProviderType
public interface StateTimeline {

    public List<StateTimeSlice> getSlices();

}