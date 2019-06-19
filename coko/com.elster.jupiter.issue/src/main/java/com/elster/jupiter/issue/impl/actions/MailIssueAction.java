package com.elster.jupiter.issue.impl.actions;

import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.MailPropertyFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.sql.SqlBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MailIssueAction extends AbstractIssueAction {
    private static final String NAME = "MailIssueAction";
    public static final String TO = NAME + ".to";

    private Issue issue;
    @Inject
    protected MailIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
    }

    @Override
    public MailIssueAction setIssue(Issue issue) {
        this.issue = issue;
        return this;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_ISSUE).format();
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        result.success(getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_ISSUE).format());
        return result;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = Arrays.asList(
                getPropertySpecService()
                        .specForValuesOf(new MailToValueFactory())
                        .named(TO, TranslationKeys.ACTION_MAIL_TO)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue(getDefaultValues())
                        .finish());
        return propertySpecs;
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(TO);
        String data = "";
        if (value != null) {
            try {
                JSONObject jsonData = new JSONObject(((MailTo) value).mailTo.get());
                data = String.format("%s",
                        jsonData.get("mailTo"));
            } catch (Exception e) {
                data = "";
            }
        }
        return data;
    }


    static class MailTo extends HasIdAndName {
        private Optional<String> mailTo;

        MailTo(String mailTo) {
            this.mailTo = mailTo != null ? Optional.of(mailTo) : Optional.empty();
        }

        @Override
        public String getId() {
            return mailTo.get();

        }

        @Override
        public String getName() {
            try {
                JSONObject jsonName = new JSONObject();
                jsonName.put("mailTo", mailTo.get());
                return jsonName.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }

    }

    private MailTo getDefaultValues() {
        MailTo mailTo = new MailTo("Recipients");
        return mailTo;
    }

    private class MailToValueFactory implements ValueFactory<MailTo>, MailPropertyFactory {
        @Override
        public MailTo fromStringValue(String stringValue) {
            MailTo m = new MailTo(stringValue);
            return m;
        }

        @Override
        public String toStringValue(MailTo object) {
            return object.getName();
        }

        @Override
        public Class<MailTo> getValueType() {
            return MailTo.class;
        }

        @Override
        public MailTo valueFromDatabase(Object object) {
            JSONObject jsonObject = new JSONObject(object);
            MailTo mailTo;
            try {
                mailTo = this.fromStringValue(jsonObject.getString("mailTo"));
            } catch (JSONException e) {
                e.printStackTrace();
                mailTo = null;
            }
            return mailTo;
        }

        @Override
        public Object valueToDatabase(MailTo object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, MailTo value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, MailTo value) {
            if (value != null) {
                builder.addObject(this.valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }

        }
    }
}