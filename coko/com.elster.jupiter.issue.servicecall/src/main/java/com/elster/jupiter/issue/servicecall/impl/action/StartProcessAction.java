/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.action;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.rest.ProcessDefinitionInfos;
import com.elster.jupiter.issue.servicecall.ServiceCallIssueService;
import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.ActionType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys.START_PROCESS_ACTION_PROCESS;

public class StartProcessAction extends AbstractIssueAction {

    private static final String NAME = START_PROCESS_ACTION_PROCESS.getKey();
    private static final String ASSOCIATION = "servicecallissue";

    private final BpmService bpmService;

    @Inject
    protected StartProcessAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, BpmService bpmService) {
        super(dataModel, thesaurus, propertySpecService);
        this.bpmService = bpmService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        Object value = properties.get(NAME);
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
            Long processId = Long.valueOf(getPropertySpec(NAME).get().getValueFactory().toStringValue(value));
            Optional<BpmProcessDefinition> connexoProcess = bpmService.getActiveBpmProcessDefinitions()
                    .stream()
                    .filter(proc -> proc.getId() == processId)
                    .findFirst();
            Map<String, Object> expectedParams = new HashMap<>();
            expectedParams.put("issueId", issue.getId());
            connexoProcess.ifPresent(bpmProcessDefinition -> bpmProcessDefinitions.processes.stream()
                    .filter(proc -> proc.name.equals(bpmProcessDefinition.getProcessName()) && proc.version.equals(bpmProcessDefinition
                            .getVersion()))
                    .forEach(p -> bpmService.startProcess(p.deploymentId, p.processId, expectedParams))
            );
            result.success(getThesaurus().getFormat(TranslationKeys.START_PROCESS_ACTION_SUCCEED).format());
        } else {
            result.fail(getThesaurus().getFormat(TranslationKeys.START_PROCESS_ACTION_FAILED).format());
        }
        return result;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        List<HasIdAndName> processInfos = bpmService.getBpmProcessDefinitions().stream().filter(this::getBpmProcessDefinitionFilter).map(ProcessInfo::new).collect(Collectors.toList());
        builder.add(
                getPropertySpecService().specForValuesOf(new ProcessInfoValueFactory())
                        .named(START_PROCESS_ACTION_PROCESS)
                        .fromThesaurus(getThesaurus())
                        .markRequired()
                        .markExhaustive()
                        .addValues(processInfos)
                        .finish());
        return builder.build();
    }

    private boolean getBpmProcessDefinitionFilter(BpmProcessDefinition processDefinition){
        return ASSOCIATION.equals(processDefinition.getAssociation());
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.START_PROCESS_ACTION).format();
    }

    @Override
    public boolean isApplicable(String reasonName){

        return super.isApplicable(reasonName) && bpmService.getActiveBpmProcessDefinitions().stream()
                .filter(this::getBpmProcessDefinitionFilter)
                .filter(f -> List.class.isInstance(f.getProperties().get("issueReasons")))
                .anyMatch(s -> ((List<Object>) s.getProperties().get("issueReasons"))
                        .stream()
                        .filter(HasIdAndName.class::isInstance)
                        .anyMatch(v -> ((HasIdAndName) v).getId().toString().equals(reasonName)));
    }

    @Override
    public long getActionType() {
        return ActionType.ACTION.getValue();
    }

    class ProcessInfoValueFactory implements ValueFactory<HasIdAndName> {

        @Override
        public HasIdAndName fromStringValue(String stringValue) {
            return new ProcessInfo(bpmService.findBpmProcessDefinition(Integer.valueOf(stringValue)).orElse(null));
        }

        @Override
        public String toStringValue(HasIdAndName object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<HasIdAndName> getValueType() {
            return HasIdAndName.class;
        }

        @Override
        public HasIdAndName valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(HasIdAndName object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, HasIdAndName value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }

    @XmlRootElement
    class ProcessInfo extends HasIdAndName {

        private BpmProcessDefinition processDefinition;

        ProcessInfo(BpmProcessDefinition processDefinition) {
            this.processDefinition = processDefinition;
        }

        @Override
        public Long getId() {
           return processDefinition.getId();
        }

        @Override
        public String getName() {
            return processDefinition.getProcessName();
        }
    }
}