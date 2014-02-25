package com.elster.jupiter.soap.whiteboard.cxf;

import com.elster.jupiter.soap.whiteboard.SoapProviderSupport;
import com.elster.jupiter.soap.whiteboard.SoapProviderSupportFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(name="com.elster.jupiter.soap.whiteboard.cxfsupport", service={SoapProviderSupportFactory.class})
public class CxfSupportFactory implements SoapProviderSupportFactory {

    @Activate
    public void activate() {

    }

    @Deactivate
    public void deactivate() {

    }

    @Override
    public SoapProviderSupport create() {
        return new CxfSupport();
    }
}
