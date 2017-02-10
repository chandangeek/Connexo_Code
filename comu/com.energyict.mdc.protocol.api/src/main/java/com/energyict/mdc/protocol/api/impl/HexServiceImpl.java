package com.energyict.mdc.protocol.api.impl;

import com.energyict.mdc.protocol.api.services.HexService;
import org.osgi.service.component.annotations.Component;

import javax.xml.bind.DatatypeConverter;

/**
 * Provides an implementation for the {@link HexService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (10:04)
 */
@Component(name = "com.energyict.mdc.hexservice", service = HexService.class, immediate = true)
public class HexServiceImpl implements HexService {

    @Override
    public String toHexString(byte[] data) {
        return DatatypeConverter.printHexBinary(data);
    }

}