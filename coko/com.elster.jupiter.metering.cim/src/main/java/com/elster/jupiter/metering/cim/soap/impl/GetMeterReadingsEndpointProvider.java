package com.elster.jupiter.metering.cim.soap.impl;

import com.elster.jupiter.soap.whiteboard.EndPointProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(name="com.elster.jupiter.metering.cim.soap", service = { EndPointProvider.class }, immediate = true, property = {"alias=cim"})
public class GetMeterReadingsEndpointProvider implements EndPointProvider {

    @Activate
    public void activate() {
        System.out.println("");
    }

    @Deactivate
    public void deactivate() {

    }

    @Override
    public Object get() {
        return new GetMeterReadingsPortImpl();
    }

}
