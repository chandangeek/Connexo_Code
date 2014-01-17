package com.energyict.protocols.mdc.services.impl;

import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.protocols.util.ProtocolUtils;
import org.osgi.service.component.annotations.Component;

/**
 * Provides an implementation for the {@link HexService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-24 (10:04)
 */
@Component(name = "com.energyict.protocols.mdc.services.hexservice", service = HexService.class)
public class HexServiceImpl implements HexService {

    @Override
    public String toHexString(byte[] data) {
        return ProtocolUtils.outputHexString(data);
    }

}