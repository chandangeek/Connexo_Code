package com.elster.jupiter.metering.cim.soap.impl;

import com.elster.jupiter.soap.whiteboard.EndPointProvider;

public class GetMeterReadingsEndpointProvider implements EndPointProvider {

    @Override
    public Object get() {
        return new GetMeterReadingsPortImpl(threadPrincipalService);
    }

}
