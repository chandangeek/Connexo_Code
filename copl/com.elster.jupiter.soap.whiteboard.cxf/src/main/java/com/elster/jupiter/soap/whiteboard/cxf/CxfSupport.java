package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.soap.whiteboard.SoapProviderSupport;
import org.apache.cxf.jaxws.spi.ProviderImpl;

public class CxfSupport implements SoapProviderSupport {

    private final ClassLoader torestore;

    public CxfSupport() {
        torestore = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(ProviderImpl.class.getClassLoader());
    }

    @Override
    public void close() {
        Thread.currentThread().setContextClassLoader(torestore);
    }
}
