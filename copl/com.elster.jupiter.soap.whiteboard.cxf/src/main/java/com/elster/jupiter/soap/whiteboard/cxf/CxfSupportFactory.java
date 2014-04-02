package com.elster.jupiter.soap.whiteboard.cxf;

import org.apache.cxf.jaxws.spi.ProviderImpl;
import org.osgi.service.component.annotations.Component;

import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import com.elster.jupiter.util.osgi.ContextClassLoaderResource;

@Component(name="com.elster.jupiter.soap.whiteboard.cxfsupport", service={SoapProviderSupportFactory.class})
public class CxfSupportFactory implements SoapProviderSupportFactory {

    @Override
    public ContextClassLoaderResource create() {
        return ContextClassLoaderResource.of(ProviderImpl.class);
    }
}
