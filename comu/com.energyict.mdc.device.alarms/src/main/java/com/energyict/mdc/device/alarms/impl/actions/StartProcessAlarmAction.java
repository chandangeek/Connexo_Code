/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.actions;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.ProcessPropertyFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.google.common.collect.ImmutableList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StartProcessAlarmAction extends AbstractIssueAction {

    private static final String NAME = "StartProcessAlarm";
    private static final String START_PROCESS = NAME + ".startprocess";

    private final IssueService issueService;
    private final UserService userService;
    private final ThreadPrincipalService threadPrincipalService;
    private final BpmService bpmService;

    private String reasonName;

    @Inject
    public StartProcessAlarmAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService, BpmService bpmService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.userService = userService;
        this.threadPrincipalService = threadPrincipalService;
        this.bpmService = bpmService;
    }

    @Override
    public boolean isApplicable(String reasonName){
        //noinspection unchecked
        return bpmService.getActiveBpmProcessDefinitions()
                .stream()
                .filter(bpmProcessDefinition -> bpmProcessDefinition.getAssociation().equals("devicealarm"))
                .filter(f -> List.class.isInstance(f.getProperties().get("alarmReasons")))
                .anyMatch(s -> ((List<Object>) s.getProperties().get("alarmReasons"))
                        .stream()
                        .filter(HasIdAndName.class::isInstance)
                        .anyMatch(v -> ((HasIdAndName) v).getId().toString().equals(reasonName)));
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_START_ALARM_PROCESS).format();
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        Object value = properties.get(START_PROCESS);
        if(value != null) {
            String jsonContent;
            JSONArray arr = null;
            String errorInvalidMessage = getThesaurus().getString("error.flow.invalid.response", "Invalid response received, please check your Flow version.");
            String errorNotFoundMessage = getThesaurus().getString("error.flow.unavailable", "Connexo Flow is not available.");
            try {
                jsonContent = bpmService.getBpmServer().doGet("/rest/deployment/processes");
                if (!"".equals(jsonContent)) {
                    JSONObject jsnobject = new JSONObject(jsonContent);
                    arr = jsnobject.getJSONArray("processDefinitionList");
                }
            } catch (JSONException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(errorInvalidMessage)
                        .build());
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(errorNotFoundMessage)
                        .build());
            }
            ProcessDefinitionInfos bpmProcessDefinitions = new ProcessDefinitionInfos(arr);
            @SuppressWarnings({"unchecked", "OptionalGetWithoutIsPresent"})
            Long processId = Long.valueOf(getPropertySpec(START_PROCESS).get().getValueFactory().toStringValue(value));
            Optional<BpmProcessDefinition> connexoProcess = bpmService.getActiveBpmProcessDefinitions()
                    .stream()
                    .filter(proc -> proc.getId() == processId)
                    .findFirst();
            Map<String, Object> expectedParams = new HashMap<>();
            expectedParams.put("alarmId", issue.getIssueId());
            connexoProcess.ifPresent(bpmProcessDefinition -> bpmProcessDefinitions.processes.stream()
                    .filter(proc -> proc.name.equals(bpmProcessDefinition.getProcessName()) && proc.version.equals(bpmProcessDefinition
                            .getVersion()))
                    .forEach(p -> bpmService.startProcess(p.deploymentId, p.processId, expectedParams)));
        }
        return result;
    }

    @Override
    public IssueAction setReasonName(String reasonName){
        this.reasonName = reasonName;
        return this;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        Process[] possibleValues = this.getPossibleStatuses();
            builder.add(
                    getPropertySpecService()
                            .specForValuesOf(new ProcessFactory())
                            .named(START_PROCESS, TranslationKeys.ACTION_START_ALARM_PROCESS)
                            .describedAs(TranslationKeys.ACTION_START_ALARM_PROCESS)
                            .fromThesaurus(getThesaurus())
                            .markRequired()
                            .setDefaultValue(possibleValues.length <= 1 ? possibleValues[0] : null)
                            .addValues(this.getPossibleStatuses())
                            .markExhaustive()
                            .finish());
        return builder.build();
    }

    private Process[] getPossibleStatuses() {
        if(reasonName != null) {
            //noinspection unchecked
            return bpmService.getActiveBpmProcessDefinitions()
                    .stream()
                    .filter(bpmProcessDefinition -> bpmProcessDefinition.getAssociation().equals("devicealarm"))
                    .filter(f -> List.class.isInstance(f.getProperties().get("alarmReasons")))
                    .filter(s -> ((List<Object>) s.getProperties().get("alarmReasons"))
                            .stream()
                            .filter(HasIdAndName.class::isInstance)
                            .anyMatch(v -> ((HasIdAndName) v).getId().toString().equals(reasonName)))
                    .map(Process::new).toArray(Process[]::new);
        }else{
            return bpmService.getActiveBpmProcessDefinitions()
                    .stream().map(Process::new).toArray(Process[]::new);
        }
    }

    private class Process extends HasIdAndName {

        private BpmProcessDefinition bpmProcess;

        public Process(BpmProcessDefinition bpmProcess){
            this.bpmProcess = bpmProcess;
        }

        @Override
        public Object getId() {
            return bpmProcess.getId();
        }

        @Override
        public String getName() {
            return bpmProcess.getProcessName();
        }
    }

    private class ProcessFactory implements ValueFactory<HasIdAndName>, ProcessPropertyFactory {

        @Override
        public Process fromStringValue(String stringValue) {
            return bpmService.getActiveBpmProcessDefinitions()
                    .stream()
                    .filter(p -> p.getId() == Long.valueOf(stringValue))
                    .findFirst()
                    .map(Process::new)
                    .orElse(null);
        }

        @Override
        public String toStringValue(HasIdAndName process) {
            return String.valueOf(process.getId());
        }

        @Override
        public Class<HasIdAndName> getValueType() {
            return HasIdAndName.class;
        }

        @Override
        public Process valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasIdAndName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws
                SQLException {

        }

        @Override
        public void bind(SqlBuilder builder, HasIdAndName value) {

        }
    }
}
