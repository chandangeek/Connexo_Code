package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.fsm.CustomStateTransitionEventType;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.StateTransitionEventType;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bvn on 2/4/16.
 */
public class ServiceCallInstaller {
    private final Logger logger = Logger.getLogger(ServiceCallInstaller.class.getName());

    private final FiniteStateMachineService finiteStateMachineService;
    private final ServiceCallService serviceCallService;
    private final DataModel dataModel;

    @Inject
    ServiceCallInstaller(FiniteStateMachineService finiteStateMachineService, ServiceCallService serviceCallService, DataModel dataModel) {
        this.finiteStateMachineService = finiteStateMachineService;
        this.serviceCallService = serviceCallService;
        this.dataModel = dataModel;
    }

    public void install() {
        try {
            this.dataModel.install(true, true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        initPrivileges();
        installDefaultLifeCycle();
    }

    private void initPrivileges() {
//        privileges.clear();
//        List<Resource> resources = userService.getResources("MDC");
//        for (Resource resource : resources) {
//            for (Privilege privilege : resource.getPrivileges()) {
//                Optional<DeviceSecurityUserAction> found = DeviceSecurityUserAction.forPrivilege(privilege.getName());
//                if (found.isPresent()) {
//                    privileges.add(privilege);
//                }
//                Optional<DeviceMessageUserAction> deviceMessageUserAction = DeviceMessageUserAction.forPrivilege(privilege.getName());
//                if(deviceMessageUserAction.isPresent()){
//                    privileges.add(privilege);
//                }
//            }
//        }

    }


    public void installDefaultLifeCycle() {
        Map<String, CustomStateTransitionEventType> eventTypes = this.findOrCreateStateTransitionEventTypes();
        this.createDefaultLifeCycle(
                TranslationKeys.DEFAULT_SERVICE_CALL_LIFE_CYCLE_NAME.getKey(),
                eventTypes);
    }

    private void createDefaultLifeCycle(String name, Map<String, CustomStateTransitionEventType> eventTypes) {
        serviceCallService.getDefaultServiceCallLifeCycle()
                .orElseGet(() -> serviceCallService.createServiceCallLifeCycle(name).create());
    }

    private Map<String, CustomStateTransitionEventType> findOrCreateStateTransitionEventTypes() {
        // Create default StateTransitionEventTypes
        this.logger.fine(() -> "Finding (or creating) default finite state machine transitions...");
        Map<String, CustomStateTransitionEventType> eventTypes = Stream
                .of(DefaultCustomStateTransitionEventType.values())
                .map(each -> each.findOrCreate(this.finiteStateMachineService))
                .collect(Collectors.toMap(
                        StateTransitionEventType::getSymbol,
                        Function.identity()));
        this.logger.fine(() -> "Found (or created) default finite state machine transitions");
        return eventTypes;
    }

}
