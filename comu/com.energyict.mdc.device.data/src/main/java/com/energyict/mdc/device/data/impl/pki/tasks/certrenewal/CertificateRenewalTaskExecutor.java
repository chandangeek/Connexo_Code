/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.pki.tasks.certrenewal;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.rest.ProcessDefinitionInfos;
import com.elster.jupiter.fsm.Stage;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceLifeCycleStatus;
import com.elster.jupiter.metering.EndDeviceStage;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.pki.CertificateWrapper;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CertificateRenewalTaskExecutor implements TaskExecutor {

    private volatile OrmService ormService;
    private volatile BpmService bpmService;
    private volatile Clock clock;

    private String certRenewalBpmProcessDefinitionId;
    private Integer certRenewalExpitationDays;
    private Integer certRenewalBpmProcessCount;
    private final Logger logger;

    private static final String CERTIFICATE_RENEWAL_PROCESS_NAME = "Certificate renewal";
    private static final String ACTIVE_STATUS = "1";

    CertificateRenewalTaskExecutor(OrmService ormService,
                                   BpmService bpmService,
                                   Clock clock,
                                   String certRenewalBpmProcessDefinitionId,
                                   Integer certRenewalExpitationDays
    ) {
        this.ormService = ormService;
        this.bpmService = bpmService;
        this.clock = clock;
        this.certRenewalBpmProcessDefinitionId = certRenewalBpmProcessDefinitionId;
        this.certRenewalExpitationDays = certRenewalExpitationDays;
        certRenewalBpmProcessCount = 0;
        logger = Logger.getAnonymousLogger();
    }

    @Override
    public void execute(TaskOccurrence occurrence) {
        logger.addHandler(occurrence.createTaskLogHandler().asHandler());
        Condition operationalDeviceCondition = Where.where("interval").isEffective(clock.instant())
                .and(Where.where("state.stage.name").isEqualTo(EndDeviceStage.OPERATIONAL.getKey()));

        Optional<DataModel> dataModel = ormService.getDataModel(MeteringService.COMPONENTNAME);
        if (!dataModel.isPresent()) {
            logger.log(Level.SEVERE, "No MTR data model found");
            return;
        }
        Subquery subquery = dataModel.get().query(EndDeviceLifeCycleStatus.class, State.class, Stage.class).asSubquery(operationalDeviceCondition, "endDevice");

        Instant expiration = clock.instant().plus(Duration.ofDays(certRenewalExpitationDays));
        Condition expiredCertificateCondition = ListOperator.IN.contains(subquery, "deviceReference.meter.id").and(
                Where.where("actualCertificate.expirationTime").isLessThanOrEqual(expiration));

        dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME);
        if (!dataModel.isPresent()) {
            logger.log(Level.SEVERE, "No DDC data model found");
            return;
        }

        List<SecurityAccessor> securityAccessors = dataModel.get()
                .query(SecurityAccessor.class, Device.class, EndDevice.class, CertificateWrapper.class)
                .select(expiredCertificateCondition)
                .stream()
                .collect(Collectors.toList());
        logger.log(Level.INFO, "Number of security accessors to process:  " + securityAccessors.size());
        printSecurityAccessors(securityAccessors,logger);

        List<SecurityAccessor> resultList = securityAccessors
                .stream()
                .filter(this::checkSecuritySets)
                .filter(SecurityAccessor::isEditable)
                .collect(Collectors.toList());

        logger.log(Level.INFO, "Number of security accessors to trigger bpm:  " + resultList.size());
        printSecurityAccessors(resultList,logger);
        resultList.forEach(securityAccessor -> triggerBpmProcess(securityAccessor, logger));
    }

    @Override
    public void postExecute(TaskOccurrence occurrence) {
        logger.log(Level.INFO, "Number of certificate renewal processes triggered  " + certRenewalBpmProcessCount);
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

    private boolean getActiveCertRenewalProcesses(SecurityAccessor securityAccessor) {
        String filter = "?variableid=deviceId&variablevalue=" + securityAccessor.getDevice().getmRID() +
                "&variableid=accessorType&variablevalue=" + securityAccessor.getKeyAccessorType().getName();
        Optional<ProcessDefinitionInfos> processDefinitionInfos = getBpmProcessDefinitions();
        if (!processDefinitionInfos.isPresent()) {
            logger.log(Level.SEVERE, "No process definitions found");
            return true;
        }
        boolean processDefinition = processDefinitionInfos.get().processes
                .stream()
                .anyMatch(processDefinitionInfo -> processDefinitionInfo.name.equalsIgnoreCase(CERTIFICATE_RENEWAL_PROCESS_NAME));
        if (!processDefinition) {
            logger.log(Level.SEVERE, "No process definition found");
            return true;
        }
        List<ProcessInstanceInfo> processInstanceInfos = bpmService.getRunningProcesses(null, filter)
                .processes
                .stream()
                .filter(p -> p.name.equalsIgnoreCase(CERTIFICATE_RENEWAL_PROCESS_NAME) && p.status.equalsIgnoreCase(ACTIVE_STATUS))
                .collect(Collectors.toList());
        if (processInstanceInfos.isEmpty()) {
            logger.log(Level.INFO, "No running processes found");
            return false;
        }
        logger.log(Level.INFO, "Found running processes for " + securityAccessor.getKeyAccessorType().getName() + " and " + securityAccessor.getDevice().getName());
        return true;
    }

    private Optional<ProcessDefinitionInfos> getBpmProcessDefinitions() {
        String jsonContent;
        JSONArray arr = null;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/rest/deployment/processes");
            if (!"".equals(jsonContent)) {
                JSONObject jsnobject = new JSONObject(jsonContent);
                arr = jsnobject.getJSONArray("processDefinitionList");
            }
        } catch (JSONException e) {
            logger.log(Level.SEVERE, "JSON error", e);
            return Optional.empty();
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Unable to connect to Flow: " + e.getMessage(), e);
            return Optional.empty();
        }
        ProcessDefinitionInfos processDefinitionInfos = new ProcessDefinitionInfos(arr);
        return Optional.of(processDefinitionInfos);
    }

    private void triggerBpmProcess(SecurityAccessor securityAccessor, Logger logger) {
        Map<String, Object> expectedParams = new HashMap<>();
        expectedParams.put("deviceId", securityAccessor.getDevice().getmRID());
        expectedParams.put("accessorType", securityAccessor.getKeyAccessorType().getName());

        Optional<BpmProcessDefinition> definition = bpmService.getAllBpmProcessDefinitions()
                .stream()
                .filter(p -> p.getProcessName().equalsIgnoreCase(CERTIFICATE_RENEWAL_PROCESS_NAME))
                .findAny();

        if (definition.isPresent()) {
            if (!getActiveCertRenewalProcesses(securityAccessor)) {
                bpmService.startProcess(definition.get(), expectedParams);
                logger.log(Level.INFO, "Certificate renewal process has been triggered on device " +
                        securityAccessor.getDevice().getName() + " mrid = " + securityAccessor.getDevice().getmRID() +
                        " for " + securityAccessor.getKeyAccessorType().getName());
                certRenewalBpmProcessCount++;
                logger.log(Level.INFO, "Number of certificate renewal processes triggered  " + certRenewalBpmProcessCount);
            }
        }
    }

}
