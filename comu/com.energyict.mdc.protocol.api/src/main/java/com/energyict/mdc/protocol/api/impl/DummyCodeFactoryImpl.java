package com.energyict.mdc.protocol.api.impl;

import com.energyict.mdc.protocol.api.codetables.Code;
import com.energyict.mdc.protocol.api.codetables.CodeFactory;

import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;

/**
 * Provides a dummy implementation for the {@link CodeFactory} interface
 * until the jupiter platform adds support for the legacy {@link Code} entity.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-29 (09:16)
 */
@Component(name="com.energyict.mdc.legacy.factory.codetable", service = CodeFactory.class)
public class DummyCodeFactoryImpl implements CodeFactory {

    @Override
    public Code findCode(int codeId) {
        return null;
    }

    @Override
    public List<Code> findAllCodeTables() {
        return Collections.emptyList();
    }

}