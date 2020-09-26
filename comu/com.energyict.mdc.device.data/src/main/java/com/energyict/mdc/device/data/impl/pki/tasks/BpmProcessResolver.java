package com.energyict.mdc.device.data.impl.pki.tasks;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessInstanceInfo;
import com.elster.jupiter.bpm.rest.ProcessDefinitionInfos;
import com.energyict.mdc.common.device.data.SecurityAccessor;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BpmProcessResolver {

    private static final String ACTIVE_STATUS = "1";

    private final Logger logger;
    private final BpmService bpmService;

    public BpmProcessResolver(BpmService bpmService, Logger logger) {
        this.bpmService = bpmService;
        this.logger = logger;
    }

    public Optional<ProcessDefinitionInfos> getBpmProcessDefinitions() {
        String jsonContent;
        JSONArray arr = null;
        try {
            jsonContent = bpmService.getBpmServer().doGet("/services/rest/server/queries/processes/definitions");
            if (!"".equals(jsonContent)) {
                JSONObject jsnobject = new JSONObject(jsonContent);
                arr = jsnobject.getJSONArray("processes");
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

    public Optional<BpmProcessDefinition> resolve(String keyRenewalBpmProcessDefinitionId) {
        List<BpmProcessDefinition> bpmProcessDefinitions = bpmService.getAllBpmProcessDefinitions();
        return bpmProcessDefinitions
                .stream()
                .filter(p -> p.getProcessName().equalsIgnoreCase(keyRenewalBpmProcessDefinitionId))
                .findAny();
    }

    /**
     * Horid piece of code but this is what we inherited...
     */
    public boolean canBeStarted(SecurityAccessor securityAccessor, String renewalBpmProcessDefinitionId) {
        String filter = "?variableid=deviceId&variablevalue=" + securityAccessor.getDevice().getmRID() +
                "&variableid=accessorType&variablevalue=" + securityAccessor.getKeyAccessorTypeReference().getName();
        List<ProcessInstanceInfo> processInstanceInfos = bpmService.getRunningProcesses(null, filter)
                .processes
                .stream()
                .filter(p -> p.name.equalsIgnoreCase(renewalBpmProcessDefinitionId) && p.status.equalsIgnoreCase(ACTIVE_STATUS))
                .collect(Collectors.toList());
        if (processInstanceInfos.isEmpty()) {
            logger.log(Level.INFO, "No running processes found");
            return true;
        }
        logger.log(Level.INFO, "Found running processes for " + securityAccessor.getKeyAccessorTypeReference().getName() + " and " + securityAccessor.getDevice().getName());
        return false;
    }


}
