/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;

public class CompletionOptionsImpl implements CompletionOptions {

    private final ServiceCall serviceCall;

    public CompletionOptionsImpl(ServiceCall serviceCall) {
        this.serviceCall = serviceCall;
    }

    @Override
    public void whenFinishedSendCompletionMessageWith(String correlationId, DestinationSpec destinationSpec) {
        CompletionOptionsServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CompletionOptionsCustomPropertySet()).get();
        domainExtension.setDestinationIdentification(correlationId);
        domainExtension.setDestinationSpec(destinationSpec.getName());
        serviceCall.update(domainExtension);
    }
}
