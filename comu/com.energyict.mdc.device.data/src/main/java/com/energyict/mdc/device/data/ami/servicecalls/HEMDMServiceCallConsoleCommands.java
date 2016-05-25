package com.energyict.mdc.device.data.ami.servicecalls;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.units.Quantity;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.BreakerStatus;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Component(name = "com.energyict.mdc.device.data.ami.servicecall.examples", service = HEMDMServiceCallConsoleCommands.class,
        property = {"osgi.command.scope=mdc.metering",
                "osgi.command.function=disconnect",
                "osgi.command.function=connect",
                "osgi.command.function=enableLoadLimit",
                "osgi.command.function=disableLoadLimit"
        }, immediate = true)
public class HEMDMServiceCallConsoleCommands {

    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile DeviceService deviceService;

    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringService meteringService;


    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Activate
    public void activate() {
        System.out.println("Activating Multisense Service Call Console Commands");
    }

    @Deactivate
    public void deactivate() {
    }


    //TODO: add date
    public void disconnect() {
        System.out.println("Usage: disconnect <mrid>");
    }

    public void disconnect(String mRID) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {

            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                    .stream()
                    .filter(cps -> cps.getCustomPropertySet()
                            .getName()
                            .equals(ServiceOperationCustomPropertySet.class.getSimpleName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find my custom property set"));

            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(ServiceCallCommands.ServiceCallTypes.disconnect.getTypeName(), ServiceCallCommands.ServiceCallTypes.disconnect.getTypeVersion())
                    .orElseGet(() -> serviceCallService.createServiceCallType(ServiceCallCommands.ServiceCallTypes.disconnect.getTypeName(), ServiceCallCommands.ServiceCallTypes.disconnect.getTypeVersion())
                            .handler("MultisenseServiceCallHandler")
                            .logLevel(LogLevel.FINEST)
                            .customPropertySet(customPropertySet)
                            .create());

            Optional<Meter> meter = meteringService.findMeter(mRID);
            ServiceOperationDomainExtension domainExtension = new ServiceOperationDomainExtension();
            domainExtension.setmRIDDevice(mRID);
            domainExtension.setActivationDate(Instant.now());
            domainExtension.setBreakerStatus(BreakerStatus.DISCONNECTED);
            domainExtension.setLoadLimitEnabled(true);
            domainExtension.setLoadLimit(Quantity.create(BigDecimal.TEN, "KW"));
            ServiceCall serviceCall;
            if(meter.isPresent()){
                serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .extendedWith(domainExtension)
                        .targetObject(meter.get())
                        .create();
                serviceCall.requestTransition(DefaultState.PENDING);
                context.commit();
            }else{
                serviceCall = serviceCallType.newServiceCall()
                        .origin("Multisense")
                        .extendedWith(domainExtension)
                        .create();
                serviceCall.log(LogLevel.SEVERE, "Meter could not be found");
                serviceCall.requestTransition(DefaultState.REJECTED);
            }

        }
    }

}
