package com.elster.jupiter.issue.share.entity.values.factories;

import com.elster.jupiter.issue.share.entity.values.AssignIssueFormValue;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.AssignIssueFormPropertyFactory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.util.sql.SqlBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class AssignIssueFormValueFactory implements ValueFactory<AssignIssueFormValue>, AssignIssueFormPropertyFactory {

    private final UserService userService;

    public AssignIssueFormValueFactory(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public AssignIssueFormValue fromStringValue(final String stringValue) {

        try {
            final JSONObject jsonData = new JSONObject(stringValue);

            final Boolean assignIssueCheckboxValue = jsonData.getBoolean("checkbox");

            final User user = userService
                    .getUser(Long.parseLong(jsonData.get("userId").toString()))
                    .orElse(null);

            final WorkGroup workgroup = userService
                    .getWorkGroup(Long.parseLong(jsonData.get("workgroupId").toString()))
                    .orElse(null);

            final String comment = jsonData.get("comment").toString();

            return new AssignIssueFormValue(assignIssueCheckboxValue, user, workgroup, comment);
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public String toStringValue(final AssignIssueFormValue object) {
        return String.valueOf(object.getId());
    }

    @Override
    public Class<AssignIssueFormValue> getValueType() {
        return AssignIssueFormValue.class;
    }

    @Override
    public AssignIssueFormValue valueFromDatabase(final Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(final AssignIssueFormValue object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(final PreparedStatement statement, final int offset, final AssignIssueFormValue value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        } else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(final SqlBuilder builder, final AssignIssueFormValue value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        } else {
            builder.addNull(Types.VARCHAR);
        }
    }
}