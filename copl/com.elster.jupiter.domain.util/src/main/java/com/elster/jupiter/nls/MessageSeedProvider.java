package com.elster.jupiter.nls;

import com.elster.jupiter.util.exception.MessageSeed;

import java.util.List;

/**
 * Provides {@link MessageSeed}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-02 (13:18)
 */
public interface MessageSeedProvider {

    Layer getLayer();

    List<MessageSeed> getSeeds();

}