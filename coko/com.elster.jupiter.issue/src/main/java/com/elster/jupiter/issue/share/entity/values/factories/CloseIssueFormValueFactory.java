package com.elster.jupiter.issue.share.entity.values.factories;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.values.CloseIssueFormValue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.CloseIssueFormPropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class CloseIssueFormValueFactory implements ValueFactory<CloseIssueFormValue>, CloseIssueFormPropertyFactory {

    private final IssueService issueService;

    public CloseIssueFormValueFactory(final IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public CloseIssueFormValue fromStringValue(final String stringValue) {

        try {
            final JSONObject jsonData = new JSONObject(stringValue);

            final Boolean closeIssueCheckboxValue = jsonData.getBoolean("checkbox");

            final IssueStatus issueStatusValue = issueService
                    .findStatus(jsonData.getString("issueStatusId"))
                    .orElse(null);

            final String commentTextareaValue = jsonData.getString("comment");

            return new CloseIssueFormValue(closeIssueCheckboxValue, issueStatusValue, commentTextareaValue);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public String toStringValue(final CloseIssueFormValue object) {
        return String.valueOf(object.getId());
    }

    @Override
    public Class<CloseIssueFormValue> getValueType() {
        return CloseIssueFormValue.class;
    }

    @Override
    public CloseIssueFormValue valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(final CloseIssueFormValue object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(final PreparedStatement statement, final int offset, final CloseIssueFormValue value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        } else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(final SqlBuilder builder, final CloseIssueFormValue value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        } else {
            builder.addNull(Types.VARCHAR);
        }
    }
}