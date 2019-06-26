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
    private IssueService issueService;
    @Inject
    protected MailIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, UserService userService, ThreadPrincipalService threadPrincipalService) {
        super(dataModel, thesaurus, propertySpecService);
        this.issueService = issueService;
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

       /* Properties props = new Properties();
        props.put("mail.host", "smtp.honeywell.com");

        Session mailConnection = Session.getInstance(props, null);
        Message msg = new MimeMessage(mailConnection);

        Address a = null;
        try {
            a = new InternetAddress("venkatakrishna.alisetty@honeywell.com", "A a");
            Address b = new InternetAddress("venkatakrishna.alisetty@honeywell.com");
            msg.setContent("Mail contect", "text/plain");
            msg.setFrom(a);
            msg.setRecipient(Message.RecipientType.TO, b);
            msg.setSubject("subject");
            Transport.send(msg);
        } catch (UnsupportedEncodingException  | MessagingException e) {
            e.printStackTrace();
        }*/



        System.out.println( "Subject:   "+issue.getIssueId() + "-"+issue.getTitle());
        System.out.println();
        System.out.println("Body:   "+"\n" +
                                "ID:" + issue.getIssueId() +"\n" +
                                "Issue reason: " + issue.getReason().getName() +"\n" +
                                "Issue type: " +issue.getReason().getIssueType().getName() +"\n" +
                                "User: " + issue.getAssignee().getUser().getDescription() +"\n" +
                                "Creation Time: " + issue.getCreateDateTime()
        );
/*
        Object ID: DLI-1012
        Issue reason: Device life cycle transition failure
        Issue type: Device life cycle
        User: root
        Creation Date: Wed 17 Apr'19 at 14:26:05*/
        //   issue.getIssueId()() + "-"+issue.getTitle()
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
                        .setDefaultValue(getDefaultValues(issue))
                        .finish());
        return propertySpecs;
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(TO);
        String data = "";
        if (value != null) {
            try {
                String recipients = ((MailTo) value).mailTo.get();
                data = String.format("%s", recipients);
            } catch (Exception e) {
                data = "";
            }
        }
        return data;
    }


    static class MailTo extends HasIdAndName {
        private Optional<String> mailTo;

        MailTo(String mailTo) {
            this.mailTo = mailTo != null ? Optional.of(mailTo) : Optional.of("");
        }

        @Override
        public String getId() {
            return mailTo.get();
        }

        @Override
        public String getName() {
            try {
                JSONObject jsonName = new JSONObject();
                jsonName.put("recipients", mailTo.get());
                return jsonName.toString();
            } catch (JSONException e) {
                e.printStackTrace();
                return "";
            }
        }

    }

    private MailTo getDefaultValues(Issue issue) {
        if(issue != null)
            return new MailTo("");
        return new MailTo("");
    }

    private class MailToValueFactory implements ValueFactory<MailTo>, MailPropertyFactory {
        @Override
        public MailTo fromStringValue(String stringValue) {
            if(stringValue.substring(0, 1).compareTo("{") == 0){
                try {
                    JSONObject jsonData = new JSONObject(stringValue);
                    String value = jsonData.get("recipients").toString();
                    return new MailTo(value);
                } catch (JSONException e) {
                    return null;
                }
            }
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
                builder.addObject(this.valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }

        }
    }
}