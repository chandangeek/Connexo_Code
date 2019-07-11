/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.actions;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.ProcessPropertyFactory;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.webservice.issue.impl.TranslationKeys;
import com.elster.jupiter.webservice.issue.impl.actions.process.WebServiceIssueProcessAssociationProvider;

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
import java.util.stream.Stream;

public class StartProcessWebServiceIssueAction extends AbstractIssueAction {
    private static final String NAME = "StartProcessWebServiceIssueAction";
    public static final String START_PROCESS = NAME + ".startprocess";

    private final BpmService bpmService;

    private String reasonName;

    @Inject
    public StartProcessWebServiceIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, BpmService bpmService) {
        super(dataModel, thesaurus, propertySpecService);
        this.bpmService = bpmService;
    }

    @Override
    public boolean isApplicable(String reasonName) {
        return bpmService.getActiveBpmProcessDefinitions()
                .stream()
                .filter(bpmProcessDefinition -> bpmProcessDefinition.getAssociation().equals(WebServiceIssueProcessAssociationProvider.ASSOCIATION_TYPE))
                .map(BpmProcessDefinition::getProperties)
                .map(properties -> properties.get(WebServiceIssueProcessAssociationProvider.PROPERTY_REASONS))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .flatMap(List::stream)
                .filter(HasIdAndName.class::isInstance)
                .map(HasIdAndName.class::cast)
                .map(HasIdAndName::getId)
                .map(Object::toString)
                .anyMatch(reason -> reason.equals(reasonName));
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_START_PROCESS).format();
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        Object value = properties.get(START_PROCESS);
        if (value != null) {
            JSONArray arr = null;
            try {
                String jsonContent = bpmService.getBpmServer().doGet("/rest/deployment/processes");
                if (!Checks.is(jsonContent).empty()) {
                    arr = new JSONObject(jsonContent).getJSONArray("processDefinitionList");
                }
            } catch (JSONException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(getThesaurus().getString("error.flow.invalid.response", "Invalid response received, please check your Flow version."))
                        .build());
            } catch (RuntimeException e) {
                throw new WebApplicationException(Response.status(Response.Status.SERVICE_UNAVAILABLE)
                        .entity(getThesaurus().getString("error.flow.unavailable", "Connexo Flow isn't available."))
                        .build());
            }
            ProcessDefinitionInfos bpmProcessDefinitions = new ProcessDefinitionInfos(arr);
            Long processId = Long.valueOf(getPropertySpec(START_PROCESS).get().getValueFactory().toStringValue(value));
            Optional<BpmProcessDefinition> connexoProcess = bpmService.findBpmProcessDefinition(processId)
                    .filter(process -> "ACTIVE".equalsIgnoreCase(process.getStatus()));
            Map<String, Object> expectedParams = new HashMap<>();
            expectedParams.put("issueId", issue.getId());
            connexoProcess.ifPresent(bpmProcessDefinition -> bpmProcessDefinitions.processes.stream()
                    .filter(proc -> proc.name.equals(bpmProcessDefinition.getProcessName()) && proc.version.equals(bpmProcessDefinition.getVersion()))
                    .forEach(p -> bpmService.startProcess(p.deploymentId, p.processId, expectedParams)));
        }
        return result;
    }

    @Override
    public IssueAction setReasonName(String reasonName) {
        this.reasonName = reasonName;
        return this;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        Process[] possibleValues = this.getPossibleProcesses();
        builder.add(
                getPropertySpecService()
                        .specForValuesOf(new ProcessFactory())
                        .named(START_PROCESS, TranslationKeys.ACTION_START_PROCESS_PROPERTY_PROCESS)
                        .fromThesaurus(getThesaurus())
                        .markRequired()
                        .setDefaultValue(possibleValues.length == 1 ? possibleValues[0] : null)
                        .addValues(possibleValues)
                        .markExhaustive()
                        .finish());
        return builder.build();
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(START_PROCESS);
        if (value != null) {
            return ((Process) value).getName();
        }
        return "";
    }

    private Process[] getPossibleProcesses() {
        Stream<BpmProcessDefinition> applicableProcesses = bpmService.getActiveBpmProcessDefinitions()
                .stream()
                .filter(bpmProcessDefinition -> bpmProcessDefinition.getAssociation().equals(WebServiceIssueProcessAssociationProvider.ASSOCIATION_TYPE));
        if (reasonName != null) {
            applicableProcesses = applicableProcesses.filter(bpmProcessDefinition -> {
                Object reasons = bpmProcessDefinition.getProperties().get(WebServiceIssueProcessAssociationProvider.PROPERTY_REASONS);
                return reasons instanceof List && ((List<?>) reasons)
                        .stream()
                        .filter(HasIdAndName.class::isInstance)
                        .map(HasIdAndName.class::cast)
                        .anyMatch(reason -> reason.getId().toString().equals(reasonName));
            });
        }
        return applicableProcesses.map(Process::new)
                .toArray(Process[]::new);
    }

    private class Process extends HasIdAndName {
        private BpmProcessDefinition bpmProcess;

        public Process(BpmProcessDefinition bpmProcess) {
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
        public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws SQLException {
        }

        @Override
        public void bind(SqlBuilder builder, HasIdAndName value) {
        }
    }
}
