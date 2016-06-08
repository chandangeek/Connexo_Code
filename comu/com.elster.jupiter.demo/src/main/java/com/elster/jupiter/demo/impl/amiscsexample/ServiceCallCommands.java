package com.elster.jupiter.demo.impl.amiscsexample;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import javax.inject.Inject;

public class ServiceCallCommands {

    protected enum ServiceCallTypes {
        arm("multisenseArmHandler", "v1.0"),
        disconnect("multiSenseDisconnectHandler", "v1.0"),
        connect("multiSenseConnectHandler", "v1.0"),
        enableLoadLimit("multisenseEnableLoadLimitHandler", "v1.0"),
        disableLoadLimit("multisenseDisableLoadLimitHandler", "v1.0");


        private final String typeName;
        private final String typeVersion;

        ServiceCallTypes(String typeName, String typeVersion) {
            this.typeName = typeName;
            this.typeVersion = typeVersion;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getTypeVersion() {
            return typeVersion;
        }
    }

    private final ServiceCallService serviceCallService;
    private final CustomPropertySetService customPropertySetService;

    private ServiceCallType serviceCallType;

    @Inject
    public ServiceCallCommands(ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService) {
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
    }

}