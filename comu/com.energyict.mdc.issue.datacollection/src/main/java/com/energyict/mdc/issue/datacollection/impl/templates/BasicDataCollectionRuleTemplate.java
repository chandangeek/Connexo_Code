/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.templates;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.RaiseEventUrgencyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfo;
import com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycleConfigurationService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;
import com.energyict.mdc.issue.datacollection.impl.i18n.TranslationKeys;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.energyict.mdc.device.config.properties.DeviceLifeCycleInDeviceTypeInfoValueFactory.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.CONNECTION_LOST;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.DEVICE_COMMUNICATION_FAILURE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNABLE_TO_CONNECT;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_INBOUND_DEVICE;
import static com.energyict.mdc.issue.datacollection.impl.event.DataCollectionEventDescription.UNKNOWN_OUTBOUND_DEVICE;

@Component(name = "com.energyict.mdc.issue.datacollection.BasicDatacollectionRuleTemplate",
        property = {"name=" + BasicDataCollectionRuleTemplate.NAME},
        service = CreationRuleTemplate.class,
        immediate = true)
public class BasicDataCollectionRuleTemplate extends AbstractDataCollectionTemplate {
    private static final Logger LOG = Logger.getLogger(BasicDataCollectionRuleTemplate.class.getName());
    static final String NAME = "BasicDataCollectionRuleTemplate";

    public static final String EVENTTYPE = NAME + ".eventType";
    public static final String AUTORESOLUTION = NAME + ".autoresolution";
    public static final String RADIOGROUP = NAME + ".increaseurgency";
    private static final String DEFAULT_VALUE = "Do nothing";
    private static final Long DEFAULT_KEY = 0L;

    //for OSGI
    public BasicDataCollectionRuleTemplate() {
    }

    @Inject
    public BasicDataCollectionRuleTemplate(IssueDataCollectionService issueDataCollectionService, NlsService nlsService, IssueService issueService, PropertySpecService propertySpecService, DeviceConfigurationService deviceConfigurationService, DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        this();
        setIssueDataCollectionService(issueDataCollectionService);
        setNlsService(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceLifeCycleConfigurationService( deviceLifeCycleConfigurationService);
        activate();
    }

    @Activate
    public void activate() {
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(IssueDataCollectionService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        super.setIssueDataCollectionService(issueDataCollectionService);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        super.setIssueService(issueService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        super.setDeviceConfigurationService(deviceConfigurationService);
    }

    @Reference
    public void setDeviceLifeCycleConfigurationService(DeviceLifeCycleConfigurationService deviceLifeCycleConfigurationService) {
        super.setDeviceLifeCycleConfigurationService(deviceLifeCycleConfigurationService);
    }

    @Override
    public String getName() {
        return BasicDataCollectionRuleTemplate.NAME;
    }

    @Override
    public String getDescription() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DATACOLLECTION_DESCRIPTION).format();
    }

    @Override
    public String getContent() {
        return "package com.energyict.mdc.issue.datacollection\n" +
                "import com.energyict.mdc.issue.datacollection.event.DataCollectionEvent;\n" +
                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.events.EventService eventService;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Basic datacollection rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == false )\n" +
                "\teval( event.hasAssociatedDeviceLifecycleStatesInDeviceTypes(\"@{" + DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES + "}\") == true )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to create issue by basic datacollection rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueCreationEvent(@{ruleId}, event);\n" +
                "end\n" +
                "rule \"Auto-resolution section @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DataCollectionEvent( eventType == \"@{" + EVENTTYPE + "}\", resolveEvent == true, @{" + AUTORESOLUTION + "} == 1 )\n" +
                "then\n" +
                "\tLOGGER.info(\"Trying to resolve issue by basic datacollection rule=@{ruleId}\");\n" +
                "\tissueCreationService.processIssueResolutionEvent(@{ruleId}, event);\n" +
                "end";
    }

    @Override
    public void updateIssue(OpenIssue openIssue, IssueEvent event) {
        if (IssueStatus.IN_PROGRESS.equals(openIssue.getStatus().getKey())) {
            openIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).orElseThrow(() ->
                    new IllegalArgumentException(TranslationKeys.ISSUE_REASON_UNKNOWN.getDefaultFormat()) {
                    }));
        }

        Optional<EventTypeInfo> newEventProps = openIssue.getRule()
                .getProperties()
                .entrySet()
                .stream()
                .filter(entry -> entry.getKey().equals(RADIOGROUP))
                .findFirst()
                .map(found -> (EventTypeInfo) found.getValue());
        if (newEventProps.isPresent() &&
                newEventProps.get().hasIncreaseUrgency()) {
            openIssue.setPriority(Priority.get(openIssue.getPriority().increaseUrgency(), openIssue.getPriority()
                    .getImpact()));
        }

        updateConnectionAttempts(openIssue, event).update();
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(updateConnectionAttempts(openIssue, event).close(issueService.findStatus(IssueStatus.RESOLVED)
                    .get()));
        }
        return issue;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        EventTypes eventTypes = new EventTypes(getThesaurus(), CONNECTION_LOST, DEVICE_COMMUNICATION_FAILURE, UNABLE_TO_CONNECT, UNKNOWN_INBOUND_DEVICE, UNKNOWN_OUTBOUND_DEVICE);
        HashMap<Long, String> possibleActionValues = getPossibleValues();

        EventTypeInfo[] possibleValues = possibleActionValues.entrySet().stream()
                .map(entry -> new EventTypeInfo(entry.getKey(), entry.getValue()))
                .toArray(EventTypeInfo[]::new);
        builder.add(propertySpecService
                .specForValuesOf(new DeviceLifeCycleInDeviceTypeInfoValueFactory(deviceConfigurationService, deviceLifeCycleConfigurationService))
                .named(DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES, TranslationKeys.DEVICE_LIFECYCLE_STATE_IN_DEVICE_TYPES)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .markMultiValued(";")
                .addValues(deviceConfigurationService.getDeviceLifeCycleInDeviceTypeInfoPossibleValues())
                .markExhaustive(PropertySelectionMode.LIST)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeValueFactory(eventTypes))
                .named(EVENTTYPE, TranslationKeys.PARAMETER_NAME_EVENT_TYPE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .addValues(eventTypes.getEventTypes())
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish());
        builder.add(propertySpecService
                .booleanSpec()
                .named(AUTORESOLUTION, TranslationKeys.PARAMETER_AUTO_RESOLUTION)
                .fromThesaurus(this.getThesaurus())
                .setDefaultValue(true)
                .finish());
        builder.add(propertySpecService
                .specForValuesOf(new EventTypeInfoValueFactory())
                .named(RADIOGROUP, TranslationKeys.PARAMETER_RADIO_GROUP)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                .setDefaultValue(new EventTypeInfo(DEFAULT_KEY, DEFAULT_VALUE))
                .addValues(possibleValues)
                .finish());
        return builder.build();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DATACOLLECTION_NAME).format();
    }

    private OpenIssue updateConnectionAttempts(OpenIssue openIssue, IssueEvent event) {
        if (openIssue instanceof OpenIssueDataCollection && event instanceof DataCollectionEvent) {
            OpenIssueDataCollection dcIssue = OpenIssueDataCollection.class.cast(openIssue);
            dcIssue.setLastConnectionAttemptTimestamp(DataCollectionEvent.class.cast(event).getTimestamp());
            dcIssue.incrementConnectionAttempt();
            return dcIssue;
        }
        return openIssue;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    private HashMap<Long, String> getPossibleValues() {
        return new HashMap<Long, String>() {{
            put(0L, getThesaurus().getFormat(TranslationKeys.PARAMETER_DO_NOTHING).format());
            put(1L, getThesaurus().getFormat(TranslationKeys.PARAMETER_INCREASE_URGENCY).format());
        }};
    }

    @XmlRootElement
    private class EventTypeInfo extends HasIdAndName {

        private Long id;
        private String name;


        public EventTypeInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }


        @Override
        public Long getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        private boolean hasIncreaseUrgency() {
            return id == 1L;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            EventTypeInfo that = (EventTypeInfo) o;

            if (!id.equals(that.id)) {
                return false;
            }
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + id.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

    }

    private class EventTypeInfoValueFactory implements ValueFactory<HasIdAndName>, RaiseEventUrgencyFactory {
        @Override
        public EventTypeInfo fromStringValue(String stringValue) {
            return new EventTypeInfo(Long.parseLong(stringValue), getPossibleValues().get(Long.parseLong(stringValue)));
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
}