package com.energyict.mdc.engine.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.EngineService;

/**
 * Extends the {@link EngineService} interface,
 * adding behavior that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (17:31)
 */
public interface ServerEngineService extends EngineService {
    Thesaurus thesaurus();
}