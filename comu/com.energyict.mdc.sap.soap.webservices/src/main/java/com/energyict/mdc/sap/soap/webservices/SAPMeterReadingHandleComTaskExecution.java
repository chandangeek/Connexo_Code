package com.energyict.mdc.sap.soap.webservices;


import com.elster.jupiter.servicecall.ServiceCall;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SAPMeterReadingHandleComTaskExecution {

    void calculateData(ServiceCall serviceCall, long comTaskExecutionId);

    void setComTaskExecutionId(ServiceCall serviceCall, long comTaskExecutionId);

    String getServiceCallTypeName();
}
