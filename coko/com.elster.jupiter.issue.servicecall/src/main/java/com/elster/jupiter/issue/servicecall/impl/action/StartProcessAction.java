/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.action;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueActionResult.DefaultActionResult;
import com.elster.jupiter.issue.share.entity.ActionType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.stream.Collectors;

public class StartProcessAction extends AbstractIssueAction {

    private static final String NAME = "StartProcessAction";

    private final IssueService issueService;
    private final BpmService bpmService;

    @Inject
    protected StartProcessAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, BpmService bpmService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.bpmService = bpmService;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        DefaultActionResult result = new DefaultActionResult();
        //todo
        return result;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        List<HasIdAndName> processInfos = bpmService.getBpmProcessDefinitions().stream().filter(this::getBpmProcessDefinitionFilter).map(ProcessInfo::new).collect(Collectors.toList());
        builder.add(
                getPropertySpecService().specForValuesOf(new ProcessInfoValueFactory())
                        .named(TranslationKeys.START_PROCESS_ACTION_PROCESS)
                        .fromThesaurus(getThesaurus())
                        .markRequired()
                        .addValues(processInfos)
                        .finish());
        return builder.build();
    }

    private boolean getBpmProcessDefinitionFilter(BpmProcessDefinition processDefinition){
        return true;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.START_PROCESS_ACTION).format();
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return super.isApplicable(issue) && (IssueStatus.OPEN.equals(issue.getStatus().getKey()) || IssueStatus.SNOOZED.equals(issue.getStatus().getKey()));
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return true;
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