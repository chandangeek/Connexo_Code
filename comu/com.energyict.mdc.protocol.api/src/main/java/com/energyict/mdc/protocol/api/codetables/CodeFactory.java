package com.energyict.mdc.protocol.api.codetables;

import java.util.List;

/**
 * Defines the behavior of a component
 * that is capable of finding {@link Code}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-09 (08:45)
 */
public interface CodeFactory {

    public Code findCode (int codeId);

    public List<Code> findAllCodeTables();

}