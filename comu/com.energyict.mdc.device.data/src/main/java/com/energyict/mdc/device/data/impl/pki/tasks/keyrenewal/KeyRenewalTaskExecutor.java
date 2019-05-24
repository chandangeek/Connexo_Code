/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.keyrenewal;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.rest.ProcessDefinitionInfos;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceLifeCycleStatus;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CryptographicType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.tasks.TaskExecutor;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.SymmetricKeyAccessor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class KeyRenewalTaskExecutor implements TaskExecutor {

    private volatile OrmService ormService;
    private volatile BpmService bpmService;
    private final EventService eventService;
    private volatile Clock clock;

    private String keyRenewalBpmProcessDefinitionId;
    private Integer keyRenewalExpitationDays;
    private Integer keyRenewalBpmProcessCount;
    private final Logger logger;

    private static final String KEY_RENEWAL_PROCESS_NAME = "Key renewal";
    private static final String ACTIVE_STATUS = "1";

    KeyRenewalTaskExecutor(OrmService ormService,
                           BpmService bpmService,
                           EventService eventService, Clock clock,
                           String keyRenewalBpmProcessDefinitionId,
                           Integer keyRenewalExpitationDays
    ) {
        this.ormService = ormService;
        this.bpmService = bpmService;
        this.eventService = eventService;
        this.clock = clock;
        this.keyRenewalBpmProcessDefinitionId = keyRenewalBpmProcessDefinitionId;
        this.keyRenewalExpitationDays = keyRenewalExpitationDays;
        keyRenewalBpmProcessCount = 0;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        Condition operationalDeviceCondition = Where.where("interval").isEffective(clock.instant())
                .and(Where.where("state.stage.name").isEqualTo(EndDeviceStage.OPERATIONAL.getKey()));

        Optional<DataModel> dataModel = ormService.getDataModel(MeteringService.COMPONENTNAME);
        if (!dataModel.isPresent()) {
            String errorMsg = "No MTR data model found";
            postFailEvent(eventService, occurrence, errorMsg);
            logger.log(Level.SEVERE, errorMsg);
            return;
        }
        Subquery subquery = dataModel.get().query(EndDeviceLifeCycleStatus.class, State.class, Stage.class).asSubquery(operationalDeviceCondition, "endDevice");

        Condition symmetricKeyContition =
                ListOperator.IN.contains(subquery, "deviceReference.meter.id")
                        .and(Where.where("keyAccessorTypeReference.keyType.cryptographicType").isEqualTo(CryptographicType.SymmetricKey));

        dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME);
        if (!dataModel.isPresent()) {
            String errorMsg = "No DDC data model found";
            postFailEvent(eventService, occurrence, errorMsg);
            logger.log(Level.SEVERE, errorMsg);
            return;
        }

        List<SecurityAccessor> securityAccessors = dataModel.get()
                .query(SecurityAccessor.class, Device.class, EndDevice.class, SecurityAccessorType.class, KeyType.class)
                .select(symmetricKeyContition)
                .stream()
                .collect(Collectors.toList());

        logger.log(Level.INFO, "Number of security accessors to process:  " + securityAccessors.size());
        printSecurityAccessors(securityAccessors, logger);
        List<SecurityAccessor> resultList = securityAccessors
                .stream()
                .filter(securityAccessor -> deviceKeyExpired((SymmetricKeyAccessor) securityAccessor))
                .filter(this::checkSecuritySets)
                .filter(SecurityAccessor::isEditable)
                .collect(Collectors.toList());

        logger.log(Level.INFO, "Number of security accessors to trigger bpm:  " + resultList.size());
        printSecurityAccessors(resultList, logger);
        resultList.forEach(securityAccessor -> triggerBpmProcess(securityAccessor, occurrence, logger));
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        logger.log(Level.INFO, "Number of key renewal processes triggered  " + keyRenewalBpmProcessCount);
    }

    private void printSecurityAccessors(List<SecurityAccessor> securityAccessors, Logger logger) {
        StringBuilder sb = new StringBuilder();
        securityAccessors.forEach(securityAccessor -> {
            sb.append("Type=" + securityAccessor.getKeyAccessorType().getName());
            sb.append(" Device=" + securityAccessor.getDevice().getName());
            sb.append('\n');
        });
        logger.log(Level.INFO, sb.toString());
    }


    private boolean deviceKeyExpired(SymmetricKeyAccessor symmetricKeyAccessor) {
        logger.log(Level.INFO, "Checking key expiration, Type=" + symmetricKeyAccessor.getKeyAccessorType().getName()
                + " Device=" + symmetricKeyAccessor.getDevice().getName());
        Optional<SymmetricKeyWrapper> symmetricKeyWrapper = symmetricKeyAccessor.getActualValue();
        if (symmetricKeyWrapper.isPresent()) {
            Optional<Instant> expirationTime = symmetricKeyWrapper.get().getExpirationTime();
            if (expirationTime.isPresent()) {
                logger.log(Level.INFO, "Expiration time " + expirationTime);
                long daysBetween = Math.abs(ChronoUnit.DAYS.between(Instant.now(), expirationTime.get()));
                logger.log(Level.INFO, "Days till expiration " + daysBetween);
                return daysBetween <= keyRenewalExpitationDays;
            }
        }
        return false;
    }

    private boolean checkSecuritySets(SecurityAccessor securityAccessor) {
        logger.log(Level.INFO, "Checking security sets, Type=" + securityAccessor.getKeyAccessorType().getName()
                + " Device=" + securityAccessor.getDevice().getName());
        long id = securityAccessor.getKeyAccessorType().getId();
        boolean result;
        List<ConfigurationSecurityProperty> configurationSecurityProperties = new ArrayList<>();
        securityAccessor.getDevice().getDeviceConfiguration().getSecurityPropertySets().forEach(
                securityPropertySet -> {
                    List<ConfigurationSecurityProperty> properties = securityPropertySet.getConfigurationSecurityProperties();
                    if (!properties.isEmpty()) {
                        configurationSecurityProperties.addAll(properties);
                    }
                }
        );
        result = configurationSecurityProperties.stream().anyMatch(property -> property.getSecurityAccessorType().getId() == id);
        logger.log(Level.INFO, result ? "Used by security set" : "Not used by security set");
        return result;
    }

    private boolean getActiveKeyRenewalProcesses(SecurityAccessor securityAccessor, TaskOccurrence taskOccurrence) {
        String filter = "?variableid=deviceId&variablevalue=" + securityAccessor.getDevice().getmRID() +
                "&variableid=accessorType&variablevalue=" + securityAccessor.getKeyAccessorType().getName();
        Optional<ProcessDefinitionInfos> processDefinitionInfos = getBpmProcessDefinitions(taskOccurrence);
        if (!processDefinitionInfos.isPresent()) {
            String errorMsg = "No process definitions found";
            postFailEvent(eventService, taskOccurrence, errorMsg);
            logger.log(Level.SEVERE, errorMsg);
            return true;
        }
        boolean processDefinition = processDefinitionInfos.get().processes
                .stream()
                .anyMatch(processDefinitionInfo -> processDefinitionInfo.name.equalsIgnoreCase(KEY_RENEWAL_PROCESS_NAME));
        if (!processDefinition) {
            String errorMsg = "No process definition found";
            postFailEvent(eventService, taskOccurrence, errorMsg);
            logger.log(Level.SEVERE, errorMsg);
            return true;
        }
        List<ProcessInstanceInfo> processInstanceInfos = bpmService.getRunningProcesses(null, filter)
                .processes
                .stream()
                .filter(p -> p.name.equalsIgnoreCase(KEY_RENEWAL_PROCESS_NAME) && p.status.equalsIgnoreCase(ACTIVE_STATUS))
                .collect(Collectors.toList());
        if (processInstanceInfos.isEmpty()) {
            logger.log(Level.INFO, "No running processes found");
            return false;
        }
        logger.log(Level.INFO, "Found running processes for " + securityAccessor.getKeyAccessorType().getName() + " and " + securityAccessor.getDevice().getName());
        return true;
    }

    private Optional<ProcessDefinitionInfos> getBpmProcessDefinitions(TaskOccurrence taskOccurrence) {
        String jsonContent;
        JSONArray arr = null;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/deployment/processes");
            if (!"".equals(jsonContent)) {
                JSONObject jsnobject = new JSONObject(jsonContent);
                arr = jsnobject.getJSONArray("processDefinitionList");
            }
        } catch (JSONException e) {
            String errorMsg = "JSON error: " + e.getLocalizedMessage();
            postFailEvent(eventService, taskOccurrence, errorMsg);
            logger.log(Level.SEVERE, errorMsg, e);
            return Optional.empty();
        } catch (RuntimeException e) {
            String errorMsg = "Unable to connect to Flow: " + e.getLocalizedMessage();
            postFailEvent(eventService, taskOccurrence, errorMsg);
            logger.log(Level.SEVERE, errorMsg, e);
            return Optional.empty();
        }
        ProcessDefinitionInfos processDefinitionInfos = new ProcessDefinitionInfos(arr);
        return Optional.of(processDefinitionInfos);
    }

    private void triggerBpmProcess(SecurityAccessor securityAccessor, TaskOccurrence taskOccurrence, Logger logger) {
        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put("deviceId", securityAccessor.getDevice().getmRID());
        expectedParams.put("accessorType", securityAccessor.getKeyAccessorType().getName());

        List<BpmProcessDefinition> bpmProcessDefinitions = bpmService.getAllBpmProcessDefinitions();
        Optional<BpmProcessDefinition> definition = bpmProcessDefinitions
                .stream()
                .filter(p -> p.getProcessName().equalsIgnoreCase(KEY_RENEWAL_PROCESS_NAME))
                .findAny();

        if (definition.isPresent()) {
            if (!getActiveKeyRenewalProcesses(securityAccessor, taskOccurrence)) {
                bpmService.startProcess(definition.get(), expectedParams);
                logger.log(Level.INFO, "Key renewal process has been triggered on device " + securityAccessor.getDevice().getName()
                        + " mrid = " + securityAccessor.getDevice().getmRID() + " for " + securityAccessor.getKeyAccessorType().getName());
                keyRenewalBpmProcessCount++;
                logger.log(Level.INFO, "Number of key renewal processes triggered  " + keyRenewalBpmProcessCount);
            }
        }
    }
}
