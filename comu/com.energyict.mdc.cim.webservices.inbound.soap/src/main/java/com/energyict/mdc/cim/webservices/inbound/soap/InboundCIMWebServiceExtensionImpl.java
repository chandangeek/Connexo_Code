package com.energyict.mdc.cim.webservices.inbound.soap;

import com.energyict.mdc.device.data.Device;

import org.osgi.service.component.annotations.Component;

import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.cim.webservices.inbound.soap.inboundwebserviceextension", service = InboundCIMWebServiceExtension.class, immediate = true, property = {
        "name=" + InboundCIMWebServiceExtensionImpl.NAME })
public class InboundCIMWebServiceExtensionImpl implements InboundCIMWebServiceExtension{

    public final static String NAME = "InboundCIMWebServiceExtension";
    private static final Logger LOGGER = Logger.getLogger(InboundCIMWebServiceExtensionImpl.class.getName());

    @Override
    public void extendMeterInfo(Device device, MeterInfo meterInfo) {
        LOGGER.log(Level.INFO, "Unsupported operation: in need to be overriden");
    }
}
