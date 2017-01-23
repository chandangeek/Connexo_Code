package com.energyict.mdc.engine.impl;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.properties.NumberLookup;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.util.Collections;
import java.util.List;

/**
 * Provides an implementation of the {@link NumberLookupExtractor} interface
 * that returns "empty" data because NumberLookup is not supported (yet) in Connexo.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (10:46)
 */
@Component(name = "com.energyict.mdc.upl.messages.legacy.number.lookup.extractor", service = {NumberLookupExtractor.class})
@SuppressWarnings("unused")
public class NumberLookupExtractorImpl implements NumberLookupExtractor {
    @Activate
    public void activate() {
        Services.numberLookupExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.numberLookupExtractor(null);
    }

    @Override
    public String id(NumberLookup numberLookup) {
        return "";
    }

    @Override
    public List<String> keys(NumberLookup numberLookup) {
        return Collections.emptyList();
    }
}