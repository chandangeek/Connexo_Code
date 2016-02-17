package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessAssociationProvider;


import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;


@Path("/processassociationproviders")
    public class ProcessAssociationProviderResource {

        @Inject
        private BpmService bpmService;

        @GET
        @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
        public List<ProcessAssociationProviderInfo> getProcessAssociationProviders() {
                return bpmService.getProcessAssociationProviders().stream()
                    .map(this::processAssociationProviderInfo).collect(Collectors.toList());
        }

        private ProcessAssociationProviderInfo processAssociationProviderInfo(ProcessAssociationProvider provider) {
            ProcessAssociationProviderInfo providerInfo = new ProcessAssociationProviderInfo();
            providerInfo.name = provider.getName();
            providerInfo.type = provider.getType();
            return providerInfo;
        }

    }
