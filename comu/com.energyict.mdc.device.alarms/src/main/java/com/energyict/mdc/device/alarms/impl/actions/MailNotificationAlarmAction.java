package com.energyict.mdc.device.alarms.impl.actions;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.MailPropertyFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class MailNotificationAlarmAction extends AbstractIssueAction {


    private static final String NAME = "MailNotificationAlarmAction";
    public static final String TO = NAME + ".to";

    private static final Logger LOGGER = Logger.getLogger(MailNotificationAlarmAction.class.getName());

    private Issue issue;

    @Inject
    public MailNotificationAlarmAction(DataModel dataModel, Thesaurus thesaurus, com.energyict.mdc.dynamic.PropertySpecService propertySpecService)
    {
        super(dataModel, thesaurus, propertySpecService);
    }


    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_NOTIFY).format();
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        result.success(getThesaurus().getFormat(TranslationKeys.ACTION_MAIL_NOTIFY).format());
        return result;
    }
    @Override
    public MailNotificationAlarmAction setIssue(Issue issue) {
        this.issue = issue;
        return this;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Arrays.asList(
                getPropertySpecService()
                        .specForValuesOf(new MailValueFactory())
                        .named(TO, TranslationKeys.ACTION_MAIL_TO)
                        .fromThesaurus(this.getThesaurus())
                        .markRequired()
                        .setDefaultValue((MailTo) issue)
                        .finish());
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(TO);
        String data = "";
        if (value != null) {
            data = ((MailTo) value).recipient.get();
        }
            return data;
    }
    public static class MailTo extends HasIdAndName {
        private Optional<String> recipient;


        public MailTo(String recipient) {

            this.recipient = recipient != null ? Optional.of(recipient) : Optional.empty();
        }

        @Override
        public Object getId() {
         return recipient.get();
        }

        @Override
        public String getName() {
                try {
                    JSONObject jsonId = new JSONObject();
                    jsonId.put("recipient", recipient.orElse(""));
                    return jsonId.toString();
                } catch (JSONException e) {
                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
                return "";
            }
        }


        private class MailValueFactory implements ValueFactory<MailTo>, MailPropertyFactory {

            @Override
            public MailTo fromStringValue(String stringValue) {
                //framework is not returning JSON string when recipient value is entered for the first time
                //Hence, this specific check for JSON string
                if (stringValue.substring(0, 1).compareTo("{") == 0) {
                    try {
                        JSONObject jsonData = new JSONObject(stringValue);
                        String recipient = jsonData.get("recipient").toString();
                        return new MailTo(recipient);
                    }
                 catch (JSONException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }}
                    return new MailTo(stringValue);
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
                return this.fromStringValue((String) object);
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
                    builder.addObject(valueToDatabase(value));
                } else {
                    builder.addNull(Types.VARCHAR);
                }
            }
        }
}



