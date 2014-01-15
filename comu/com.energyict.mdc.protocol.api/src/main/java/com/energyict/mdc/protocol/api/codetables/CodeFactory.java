package com.energyict.mdc.protocol.api.codetables;

import com.energyict.mdc.common.ApplicationComponent;

/**
 * Defines the behavior of an {@link ApplicationComponent}
 * that is capable of finding {@link Code}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-09 (08:45)
 */
public interface CodeFactory {

    public Code findCode (int codeId);

}