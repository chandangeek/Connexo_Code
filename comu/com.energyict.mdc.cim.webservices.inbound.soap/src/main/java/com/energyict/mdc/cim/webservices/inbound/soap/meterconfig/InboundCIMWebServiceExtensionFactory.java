package com.energyict.mdc.cim.webservices.inbound.soap.meterconfig;

import com.energyict.mdc.cim.webservices.inbound.soap.InboundCIMWebServiceExtension;

import java.util.Optional;

public class InboundCIMWebServiceExtensionFactory {

    private volatile Optional<InboundCIMWebServiceExtension> inboundCIMWebServiceExtension = Optional.empty();

    public void setWebServiceExtension(InboundCIMWebServiceExtension webServiceExtension) {
        this.inboundCIMWebServiceExtension = Optional.of(webServiceExtension);
    }

    public void unsetWebServiceExtension(InboundCIMWebServiceExtension webServiceExtension) {
        this.inboundCIMWebServiceExtension = Optional.empty();
    }

    public Optional<InboundCIMWebServiceExtension> getWebServiceExtension()
    {
        return this.inboundCIMWebServiceExtension;
    }
}
