/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.impl.webservicecall;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.impl.DestinationFailedException;
import com.elster.jupiter.export.impl.IDataExportService;
import com.elster.jupiter.export.impl.MessageSeeds;
import com.elster.jupiter.export.webservicecall.DataExportServiceCallType;
import com.elster.jupiter.export.webservicecall.ServiceCallStatus;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component(name = WebServiceDataExportServiceCallHandler.NAME,
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=" + WebServiceDataExportServiceCallHandler.NAME)
public class WebServiceDataExportServiceCallHandler implements ServiceCallHandler {
    public static final int CHECK_PAUSE_IN_SECONDS = 10;
    static final String NAME = "WebServiceDataExportServiceCallHandler";

    private volatile Thesaurus thesaurus;
    private volatile CustomPropertySet<ServiceCall, WebServiceDataExportDomainExtension> serviceCallCPS;
    private volatile DataExportServiceCallType dataExportServiceCallType;

    public WebServiceDataExportServiceCallHandler() {
        // for OSGi purposes
    }

    @Inject
    public WebServiceDataExportServiceCallHandler(Thesaurus thesaurus, DataExportServiceCallType dataExportServiceCallType,
                                                  CustomPropertySet<ServiceCall, WebServiceDataExportDomainExtension> serviceCallCPS) {
        this.thesaurus = thesaurus;
        this.dataExportServiceCallType = dataExportServiceCallType;
        this.serviceCallCPS = serviceCallCPS;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(DataExportService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setDataExportService(IDataExportService dataExportService) {
        serviceCallCPS = dataExportService.getServiceCallCPS();
        dataExportServiceCallType = dataExportService.getDataExportServiceCallType();
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case ONGOING:
                process(serviceCall);
                break;
            default:
                // No specific action required for these states
                break;
        }
    }

    private void process(ServiceCall serviceCall) {
        try {
            WebServiceDataExportDomainExtension serviceCallProperties = serviceCall.getExtensionFor(serviceCallCPS)
                    .orElseThrow(() -> new DestinationFailedException(thesaurus, MessageSeeds.NO_CPS_VALUES_FOUND, serviceCall.getNumber()));
            CompletableFuture.supplyAsync(() -> checkStateUntilClosed(serviceCall), Executors.newSingleThreadExecutor())
                    .get(serviceCallProperties.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            // the time is out; no response
            dataExportServiceCallType.tryFailingServiceCall(serviceCall,
                    thesaurus.getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_NO_CONFIRMATION).format());
        } catch (CompletionException | ExecutionException e) {
            dataExportServiceCallType.tryFailingServiceCall(serviceCall,
                    thesaurus.getSimpleFormat(MessageSeeds.WEB_SERVICE_EXPORT_WAITING_FAILURE).format(e.getCause().getLocalizedMessage()));
        } catch (Exception e) {
            dataExportServiceCallType.tryFailingServiceCall(serviceCall, e.getLocalizedMessage());
        }
    }

    private ServiceCallStatus checkStateUntilClosed(ServiceCall serviceCall) {
        ServiceCallStatus status;
        do {
            try {
                Thread.sleep(1000L * CHECK_PAUSE_IN_SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            status = dataExportServiceCallType.getStatus(serviceCall);
        } while (status.isOpen());
        return status;
    }
}
