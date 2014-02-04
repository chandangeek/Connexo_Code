package com.elster.jupiter.metering.cim.soap.impl;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.soap.whiteboard.EndPointProvider;
import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(name="com.elster.jupiter.metering.cim.soap", service = { EndPointProvider.class }, immediate = true, property = {"alias=/cim"})
public class GetMeterReadingsEndpointProvider implements EndPointProvider {

    private volatile MeteringService meteringService;
    private volatile Clock clock;

    @Activate
    public void activate() {
        System.out.println("");
    }

    @Deactivate
    public void deactivate() {

    }

    @Override
    public Object get() {
        return new GetMeterReadingsPortImpl(meteringService, clock);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }
}
