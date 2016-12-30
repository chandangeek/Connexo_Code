package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.properties.NumberLookup;

import java.util.Collections;
import java.util.List;

/**
 * Provides an dummy implementation for the {@link Extractor} interface
 * that returns empty values in all of its methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-28 (09:49)
 */
final class DummyNumberLookupExtractor implements NumberLookupExtractor {
    @Override
    public String id(NumberLookup numberLookup) {
        return "";
    }

    @Override
    public List<String> keys(NumberLookup numberLookup) {
        return Collections.emptyList();
    }
}