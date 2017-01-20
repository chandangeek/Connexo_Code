package com.energyict.mdc.device.alarms.impl.templates;

import com.elster.jupiter.issue.share.CreationRuleTemplate;
import com.elster.jupiter.issue.share.IssueEvent;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.OpenDeviceAlarm;
import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;
import com.energyict.mdc.device.alarms.event.EndDeviceEventCreatedEvent;
import com.energyict.mdc.device.alarms.impl.event.DeviceAlarmEventDescription;
import com.energyict.mdc.device.alarms.impl.i18n.TranslationKeys;
import com.energyict.mdc.dynamic.PropertySpecService;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.device.alarms.BasicDeviceAlarmRuleTemplate",
        property = {"name=" + BasicDeviceAlarmRuleTemplate.NAME},
        service = CreationRuleTemplate.class,
        immediate = true)
public class BasicDeviceAlarmRuleTemplate extends AbstractDeviceAlarmTemplate {
    protected static final Logger LOG = Logger.getLogger(BasicDeviceAlarmRuleTemplate.class.getName());
    static final String NAME = "BasicDeviceAlarmRuleTemplate";
    public static final String EVENTTYPE = NAME + ".eventType";
    public static final String LOG_ON_SAME_ALARM = NAME + ".logOnSameAlarm";
    public static final String TRIGGERING_EVENTS = NAME + ".triggeringEvents";
    public static final String CLEARING_EVENTS = NAME + ".clearingEvents";
    //public static final String THRESHOLD_TYPE = NAME + ".tresholdType";
    //public static final String THRESHOLD_VALUE = NAME + ".tresholdValue";
    public static final String THRESHOLD = NAME + ".threshold";
    public static final String UP_URGENCY_ON_RAISE = NAME + ".upUrgencyOnRaise";
    public static final String DOWN_URGENCY_ON_CLEAR = NAME + ".downUrgencyOnClear";
    public static final String EVENT_OCCURENCE_COUNT = NAME + ".eventCount";
    public static final String DEVICE_LIFECYCLE_STATE = NAME + ".deviceLifecyle";
    public static final String DEVICE_TYPES = NAME + ".deviceTypes";
    public static final String EIS_CODES = NAME + ".eisCodes";

    private String SEPARATOR = ":";

    //for OSGI
    public BasicDeviceAlarmRuleTemplate() {
    }

    @Inject
    public BasicDeviceAlarmRuleTemplate(DeviceAlarmService deviceAlarmService, NlsService nlsService, IssueService issueService, PropertySpecService propertySpecService) {
        this();
        setDeviceAlarmService(deviceAlarmService);
        setNlsService(nlsService);
        setIssueService(issueService);
        setPropertySpecService(propertySpecService);

        activate();
    }

    @Activate
    public void activate() {
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.setThesaurus(nlsService.getThesaurus(DeviceAlarmService.COMPONENT_NAME, Layer.DOMAIN));
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        super.setDeviceAlarmService(deviceAlarmService);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        super.setIssueService(issueService);
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        super.setPropertySpecService(propertySpecService);
    }

    @Override
    public String getName() {
        return BasicDeviceAlarmRuleTemplate.NAME;
    }

    @Override
    public String getDescription() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DEVICE_ALARM_DESCRIPTION).format();
    }

    //END_DEVICE_EVENT_CREATED
    @Override
    public String getContent() {
        return "package com.energyict.mdc.device.device.alarms\n" +
                "import com.energyict.mdc.device.alarms.event.DeviceAlarmEvent;\n" +

                "global java.util.logging.Logger LOGGER;\n" +
                "global com.elster.jupiter.issue.share.service.IssueCreationService issueCreationService;\n" +
                "rule \"Basic device alarm rule @{ruleId}\"\n" +
                "when\n" +
                "\tevent : DeviceAlarmEvent( eventType == \"@{" + EVENTTYPE + "}\" )\n" +
                // maybe both TRIGGERING_EVENTS + CLEARING_EVENTS
                "\teval( event.computeOccurenceCount(\"@{" + THRESHOLD + "}\", \"@{" + TRIGGERING_EVENTS + "}\", \"@{" + DEVICE_TYPES + "}\", \"@{" + EIS_CODES + "}\") >= @{" + EVENT_OCCURENCE_COUNT + "} )\n" +
                "\teval( event.getAssociatedDeviceLifecycleState() == @{" + DEVICE_LIFECYCLE_STATE + "} )\n" +
                "then\n" +
                "\tSystem.out.println(\"Generating device alarm @{ruleId}\");\n" +
                //  "\tboolean clearing = event.isClearing();\n" +
                "\tissueCreationService.processAlarmCreationEvent(@{ruleId}, event," + "@{" + LOG_ON_SAME_ALARM + "});\n" +
                "end";
    }

    @Override
    public void updateIssue(OpenIssue openIssue, IssueEvent event) {
        if (IssueStatus.IN_PROGRESS.equals(openIssue.getStatus().getKey())) {
            openIssue.setStatus(issueService.findStatus(IssueStatus.OPEN).get());
        }
        getAlarmForUpdate(openIssue, event).update();
    }

    @Override
    public Optional<? extends Issue> resolveIssue(IssueEvent event) {
        //TODO - resolve all occurrences
        Optional<? extends Issue> issue = event.findExistingIssue();
        if (issue.isPresent() && !issue.get().getStatus().isHistorical()) {
            OpenIssue openIssue = (OpenIssue) issue.get();
            issue = Optional.of(getAlarmForClosure(openIssue, event).close(issueService.findStatus(IssueStatus.RESOLVED).get()));
        }
        return issue;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Builder<PropertySpec> builder = ImmutableList.builder();
        EventTypes eventTypes = new EventTypes(getThesaurus(), DeviceAlarmEventDescription.values());
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
                .named(LOG_ON_SAME_ALARM, TranslationKeys.LOG_ON_SAME_ALARM)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                //.markExhaustive()
                .finish());
        builder.add(propertySpecService
                .longSpec()
                .named(THRESHOLD, TranslationKeys.EVENT_TEMPORAL_THRESHOLD)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                //.markExhaustive()
                .finish());
        builder.add(propertySpecService
                .longSpec()
                .named(EVENT_OCCURENCE_COUNT, TranslationKeys.EVENT_OCCURENCE_COUNT)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(TRIGGERING_EVENTS, TranslationKeys.TRIGGERING_EVENTS)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(CLEARING_EVENTS, TranslationKeys.CLEARING_EVENTS)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(DEVICE_LIFECYCLE_STATE, TranslationKeys.DEVICE_LIFECYCLE_STATE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(DEVICE_TYPES, TranslationKeys.DEVICE_TYPES)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(EIS_CODES, TranslationKeys.EIS_CODES)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(UP_URGENCY_ON_RAISE, TranslationKeys.UP_URGENCY_ON_RAISE)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        builder.add(propertySpecService
                .stringSpec()
                .named(DOWN_URGENCY_ON_CLEAR, TranslationKeys.DOWN_URGENCY_ON_CLEAR)
                .fromThesaurus(this.getThesaurus())
                .markRequired()
                // .markExhaustive()
                .finish());
        return builder.build();
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.BASIC_TEMPLATE_DEVICE_ALARM_NAME).format();
    }

    private OpenIssue getAlarmForUpdate(OpenIssue openIssue, IssueEvent event) {
        if (openIssue instanceof OpenDeviceAlarm && event instanceof DeviceAlarmEvent) {
            OpenDeviceAlarm alarm = OpenDeviceAlarm.class.cast(openIssue);
            Optional<String> clearingEvents = alarm.getRule().getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(CLEARING_EVENTS))
                    .findFirst().map(found -> (String) found.getValue());
            Optional<String> upUrgencyOnRaise = alarm.getRule().getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(UP_URGENCY_ON_RAISE))
                    .findFirst().map(found -> (String) found.getValue());
            Optional<String> downUrgencyOnClear = alarm.getRule().getProperties().entrySet().stream().filter(entry -> entry.getKey().equals(DOWN_URGENCY_ON_CLEAR))
                    .findFirst().map(found -> (String) found.getValue());
            if (clearingEvents.isPresent() && ((DeviceAlarmEvent) event).isClearing(clearingEvents.get()) && !alarm.isStatusCleared()) {
                alarm.setClearedStatus();
                if (downUrgencyOnClear.isPresent() && Integer.parseInt(downUrgencyOnClear.get()) == 1) {
                    if (!alarm.getPriority().lowerUrgency()) {
                        LOG.log(Level.SEVERE, "Urgency is minimum [" + alarm.getPriority().getUrgency() +"]. Unable to decrement anymore");
                    }
                }
            }
            if (upUrgencyOnRaise.isPresent() && Integer.parseInt(upUrgencyOnRaise.get()) == 1) {
                if (!alarm.getPriority().increaseUrgency()) {
                    LOG.log(Level.SEVERE, "Urgency is maximum [" + alarm.getPriority().getUrgency() +"]. Unable to increment anymore");
                }
            }
            alarm.addRelatedAlarmEvent(alarm.getDevice().getId(), ((EndDeviceEventCreatedEvent) event).getEventTypeMrid(), ((EndDeviceEventCreatedEvent) event).getTimestamp());
            return alarm;
        }
        return openIssue;
    }

    private OpenIssue getAlarmForClosure(OpenIssue openIssue, IssueEvent event) {
        if (openIssue instanceof OpenDeviceAlarm && event instanceof DeviceAlarmEvent) {
            return OpenDeviceAlarm.class.cast(openIssue);
        }
        return openIssue;
    }

    //TODO - write a check method to avoid number format exceptions
    /*
    public static boolean isInteger(String s) {
    try {
        Integer.parseInt(s);
    } catch(NumberFormatException e) {
        return false;
    } catch(NullPointerException e) {
        return false;
    }
    return true;
}
     */
}