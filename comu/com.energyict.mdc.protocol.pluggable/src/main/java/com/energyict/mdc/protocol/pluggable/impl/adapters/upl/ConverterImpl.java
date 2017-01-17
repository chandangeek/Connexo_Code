package com.energyict.mdc.protocol.pluggable.impl.adapters.upl;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.HexString;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link Converter} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (13:13)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable.upl.security", service = {Converter.class})
@SuppressWarnings("unused")
public class ConverterImpl implements Converter {

    @Activate
    public void activate() {
        Services.converter(this);
    }

    @Override
    public HexString hexFromString(String value) {
        return new com.energyict.mdc.common.HexString(value);
    }

}