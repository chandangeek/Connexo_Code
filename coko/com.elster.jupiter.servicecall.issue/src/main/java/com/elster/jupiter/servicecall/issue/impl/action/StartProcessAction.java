/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.action;

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
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.issue.impl.i18n.TranslationKeys;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class StartProcessAction extends AbstractIssueAction {

    private static final String NAME = "StartProcessAction";

    private final IssueService issueService;
    private final ThreadPrincipalService threadPrincipalService;

    @Inject
    protected StartProcessAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
        this.threadPrincipalService = threadPrincipalService;
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
        List<HasIdAndName> fakeProcessInfos = new ArrayList<>();
        fakeProcessInfos.add(new ProcessInfo(1, "Fake process 1"));
        fakeProcessInfos.add(new ProcessInfo(2, "Fake process 2"));
        fakeProcessInfos.add(new ProcessInfo(3, "Fake process 3"));

        builder.add(
                getPropertySpecService().specForValuesOf(new ProcessInfoValueFactory())
                        .named(TranslationKeys.START_PROCESS_ACTION_PROCESS)
                        .fromThesaurus(getThesaurus())
                        .markRequired()
                        .addValues(fakeProcessInfos)
                        .finish());
        return builder.build();
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
            return new ProcessInfo(1, stringValue);
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

        private String name;
        private int id;

        ProcessInfo(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public Integer getId() {
           return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this != o) {
                return false;
            }
            if (getClass() != o.getClass()) {
                return false;
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Integer.hashCode(id);
            return result;
        }
    }

}