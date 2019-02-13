/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.outbound.soap.sendmeterconfig;

import com.elster.jupiter.nls.Thesaurus;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
@Component(
        name = "com.energyict.mdc.cim.webservices.outbound.soap.WebServiceActivator",
        service = {WebServiceActivator.class},
        property = {"name=" + WebServiceActivator.COMPONENT_NAME},
        immediate = true)
public class WebServiceActivator {
    public static final String COMPONENT_NAME = "CIM";
    private volatile Thesaurus thesaurus;

    public WebServiceActivator() {
        // for OSGI purposes
    }

    @Inject
    public WebServiceActivator(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public Thesaurus getThesaurus() {
        return thesaurus;
    }
}
