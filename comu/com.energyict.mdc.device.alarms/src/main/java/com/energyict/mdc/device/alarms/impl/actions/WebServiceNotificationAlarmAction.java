/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.actions;

import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.IssueWebServiceClient;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.WebServicesEndPointFactory;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.impl.DeviceAlarmServiceImpl;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.google.common.collect.ImmutableList;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by H165696 on 9/5/2017.
 */
public class WebServiceNotificationAlarmAction extends AbstractIssueAction {

    private static final String NAME = "WebServiceNotificationAlarmAction";
    public static final String WEBSERVICE = NAME + ".webService";
    public static final String CLOSE = NAME + ".close";

    private DeviceAlarmService deviceAlarmService;
    private IssueService issueService;
    private UserService userService;
    private IssueWebServiceClient issueWebServiceClient;
    private ThreadPrincipalService threadPrincipalService;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public WebServiceNotificationAlarmAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService, IssueService issueService, DeviceAlarmService deviceAlarmService, UserService userService, ThreadPrincipalService threadPrincipalService, EndPointConfigurationService endPointConfigurationService) {
        super(dataModel, thesaurus, propertySpecService);
        this.deviceAlarmService = deviceAlarmService;
        this.issueService = issueService;
        this.userService = userService;
        this.threadPrincipalService = threadPrincipalService;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setIssueWebServiceClient(IssueWebServiceClient issueWebServiceClient) {
        this.issueWebServiceClient = issueWebServiceClient;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        Optional<EndPointConfiguration> endPointConfiguration = getEndPointConfiguration();
        Optional<Boolean> closeAlarm = getCloseAlarm();

        endPointConfiguration.ifPresent(endPointConfig -> ((DeviceAlarmServiceImpl) deviceAlarmService).getIssueWebServiceClients()
                .stream().filter(alarmWebServiceClient -> alarmWebServiceClient.getWebServiceName().equals(endPointConfig.getWebServiceName()))
                .findFirst().ifPresent(alarmWebServiceClient1 -> {
                    if (alarmWebServiceClient1.call(issue, endPointConfig)) {
                        closeAlarm.ifPresent(close -> {
                            if (close == true) {
                                ((OpenIssue) issue).close(issueService.findStatus(IssueStatus.FORWARDED).get());
                            }
                        });
                    }
                    result.success(getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CALLED).format());
                }));
        return result;
    }

    private Optional<EndPointConfiguration> getEndPointConfiguration() {
        Object value = properties.get(WEBSERVICE);
        if (value != null) {
            return Optional.ofNullable(((EndPoint) value).endPointConfiguration);
        }
        return Optional.empty();
    }

    private Optional<Boolean> getCloseAlarm() {
        Object value = properties.get(CLOSE);
        if (value != null) {
            return Optional.ofNullable(((Boolean) properties.get(CLOSE)));
        }
        return Optional.empty();
    }


    @Override
    public List<PropertySpec> getPropertySpecs() {
        ImmutableList.Builder<PropertySpec> builder = ImmutableList.builder();
        EndPoint[] possibleValues = this.getPossibleStatuses();
        builder.add(
                getPropertySpecService()
                        .specForValuesOf(new EndPointConfigurationFactory())
                        .named(WEBSERVICE, TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION)
                        .describedAs(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION)
                        .fromThesaurus(getThesaurus())
                        .markRequired()
                        .setDefaultValue(possibleValues.length == 1 ? possibleValues[0] : null)
                        .addValues(possibleValues)
                        .markExhaustive()
                        .finish());

        Map<String, String> description = new HashMap<>();
        description.put("tooltip", getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ALARM_DESCRIPTION).format());

        BasicPropertySpec closeItem = new BasicPropertySpec(new BooleanFactory());
        closeItem.setName(CLOSE);
        closeItem.setDisplayName(getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION_CLOSE_ALARM).format());
        closeItem.setDescription(new JSONObject(description).toString());
        builder.add(closeItem);

        return builder.build();
    }

    private EndPoint[] getPossibleStatuses() {
        return endPointConfigurationService.findEndPointConfigurations().stream()
                .filter(EndPointConfiguration::isActive)
                .filter(endPointConfiguration ->
                        ((DeviceAlarmServiceImpl) deviceAlarmService).getIssueWebServiceClients().stream()
                                .filter(issueWebServiceClient1 -> issueWebServiceClient1.getWebServiceName().compareTo(endPointConfiguration.getWebServiceName()) == 0)
                                .count() > 0)
                .map(EndPoint::new).toArray(EndPoint[]::new);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.ACTION_WEBSERVICE_NOTIFICATION).format();
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return super.isApplicableForUser(user) && user.getPrivileges().stream().anyMatch(p -> Privileges.Constants.ACTION_ISSUE.equals(p.getName()));
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return issue == null;
    }

    @Override
    public String getFormattedProperties(Map<String, Object> props) {
        Object value = props.get(WEBSERVICE);
        if (value != null) {
            return Optional.ofNullable(((WebServiceNotificationAlarmAction.EndPoint) value).endPointConfiguration).get().getName();
        }
        return "";
    }

    public static class EndPoint extends HasIdAndName {

        private EndPointConfiguration endPointConfiguration;

        public EndPoint(EndPointConfiguration endPointConfiguration) {
            this.endPointConfiguration = endPointConfiguration;
        }

        @Override
        public Object getId() {
            return endPointConfiguration.getId();
        }

        @Override
        public String getName() {
            return endPointConfiguration.getName();
        }
    }

    private class EndPointConfigurationFactory implements ValueFactory<EndPoint>, WebServicesEndPointFactory {
        @Override
        public EndPoint fromStringValue(String stringValue) {
            return endPointConfigurationService.findEndPointConfigurations()
                    .stream()
                    .filter(p -> p.getId() == Long.valueOf(stringValue))
                    .findFirst()
                    .map(EndPoint::new)
                    .orElse(null);
        }

        @Override
        public String toStringValue(EndPoint endPoint) {
            return String.valueOf(endPoint.getId());
        }

        @Override
        public Class<EndPoint> getValueType() {
            return EndPoint.class;
        }

        @Override
        public EndPoint valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(EndPoint object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, EndPoint value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, EndPoint value) {
            if (value != null) {
                builder.addObject(this.valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }
}