/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.autoreschedule.impl;

import com.elster.jupiter.customtask.CustomTaskAction;
import com.elster.jupiter.customtask.CustomTaskFactory;
import com.elster.jupiter.customtask.CustomTaskProperty;
import com.elster.jupiter.customtask.PropertiesInfo;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.BpmProcessPropertyFactory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.autoreschedule.security.Privileges;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component(
        name = "com.energyict.mdc.autoreschedule.impl.AutoRescheduleTaskFactory",
        service = CustomTaskFactory.class,
        property = "name=" + MessageSeeds.COMPONENT_NAME,
        immediate = true)
public class AutoRescheduleTaskFactory implements CustomTaskFactory {
    static final String SUBSCRIBER_NAME = "RetryFailedComTasks";
    static final String DESTINATION_NAME = "RetryFailedComTasks";
    static final String SUBSCRIBER_DISPLAY_NAME = "Handle Retry Failed Communication tasks";
    static final String DISPLAY_NAME = "Retry failed communication tasks";

    public static final String NAME = "RetryFailedComTasks";
    public static final String COM_TASKS_SEPARATOR = ";";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile TaskService taskService;

    public AutoRescheduleTaskFactory() {
    }

    @Inject
    public AutoRescheduleTaskFactory(NlsService nlsService, PropertySpecService propertySpecService, MeteringGroupsService meteringGroupsService,
                                     TaskService taskService) {
        this();
        setPropertySpecService(propertySpecService);
        setThesaurus(nlsService);
        setMeteringGroupsService(meteringGroupsService);
        setTaskService(taskService);
    }

    @Reference
    void setThesaurus(NlsService nlsService) {
        thesaurus = nlsService.getThesaurus(MessageSeeds.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Reference
    void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Override
    public boolean isValid(List<CustomTaskProperty> properties, ConstraintValidatorContext context) {
        boolean valid = true;
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(getEndDeviceGroupSelectorProperty());
        propertySpecs.add(getComTaskSelectorProperty());
        for (PropertySpec propertySpec : propertySpecs) {
            try {
                Object value = properties.stream()
                        .filter(customTaskProperty -> customTaskProperty.getName().compareToIgnoreCase(propertySpec.getName()) == 0)
                        .findFirst().map(customTaskProperty -> customTaskProperty.getValue()).orElse(null);

                propertySpec.validateValue(value);
            } catch (InvalidValueException e) {
                valid = false;
                context.buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), getNameFromArguments(e.getArguments())))
                        .addPropertyNode("properties." + propertySpec.getName()).addConstraintViolation()
                        .disableDefaultConstraintViolation();
            }
        }
        return valid;
    }

    private String getNameFromArguments(Object[] key) {
        String name = "";
        if (key == null) {
            return name;
        }
        String keyString = key[0].toString();
        for (TranslationKeys translationKey : TranslationKeys.values()) {
            if (translationKey.getKey().equals(keyString)) {
                name = translationKey.getDefaultFormat();
            }
        }
        return name;
    }

    @Override
    public List<PropertiesInfo> getProperties() {
        return Arrays.asList(
                new PropertiesInfo("dataselection", "Data selector", Arrays.asList(
                        getEndDeviceGroupSelectorProperty(),
                        getComTaskSelectorProperty()))
        );
    }

    @Override
    public List<CustomTaskAction> getActionsForUser(User user, String application) {
        List<CustomTaskAction> customTaskActions = new ArrayList<>();

        user.getPrivileges(application).stream()
                .filter(privilege -> privilege.getName().compareToIgnoreCase(Privileges.Constants.ADMINISTRATE_RETRY_FAILED_COMTASKS) == 0)
                .forEach(privilege -> customTaskActions.addAll(Arrays.asList(CustomTaskAction.ADMINISTRATE, CustomTaskAction.EDIT)));

        user.getPrivileges(application).stream()
                .filter(privilege -> privilege.getName().compareToIgnoreCase(Privileges.Constants.VIEW_RETRY_FAILED_COMTASKS) == 0)
                .forEach(p -> customTaskActions.addAll(Arrays.asList(CustomTaskAction.VIEW, CustomTaskAction.VIEW_HISTORY, CustomTaskAction.VIEW_HISTORY_LOG)));

        user.getPrivileges(application).stream()
                .filter(privilege -> privilege.getName().compareToIgnoreCase(Privileges.Constants.RUN_RETRY_FAILED_COMTASKS) == 0)
                .forEach(p -> customTaskActions.add(CustomTaskAction.RUN));
        return customTaskActions;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public List<String> targetApplications() {
        return Collections.singletonList("MDC");
    }

    private PropertySpec getEndDeviceGroupSelectorProperty() {
        List<String> allGroups = meteringGroupsService.getEndDeviceGroupQuery()
                .select(Condition.TRUE, Order.ascending("upper(name)"))
                .stream()
                .map(endDeviceGroup -> new String(endDeviceGroup.getName()))
                .collect(Collectors.toList());

        return propertySpecService
                .specForValuesOf(new StringFactory())
                .named(TranslationKeys.END_DEVICE_GROUP_SELECTOR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues(allGroups)
                .setDefaultValue(allGroups.size() == 1 ? allGroups.get(0) : null)
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
    }

    private PropertySpec getComTaskSelectorProperty() {
        List<ComTaskInfo> allComTasks = taskService.findAllComTasks()
                .find()
                .stream()
                .filter(ComTask::isUserComTask)
                .map(ComTaskInfo::new)
                .collect(Collectors.toList());

        return propertySpecService
                .specForValuesOf(new ComTaskInfoValueFactory())
                .named(TranslationKeys.COMTASK_SELECTOR.getKey(), TranslationKeys.COMTASK_SELECTOR)
                .fromThesaurus(thesaurus)
                .markRequired()
                .addValues(allComTasks)
                .markMultiValued(", ")
                .markExhaustive()
                .finish();
    }

    @XmlRootElement
    static class ComTaskInfo extends HasIdAndName {

        private transient ComTask comTask;

        ComTaskInfo(ComTask comTask) {
            this.comTask = comTask;
        }

        @Override
        public Long getId() {
            return comTask.getId();
        }

        @Override
        public String getName() {
            return comTask.getName();
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

            ComTaskInfo that = (ComTaskInfo) o;

            return comTask.getId() == that.comTask.getId();

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + Long.hashCode(comTask.getId());
            return result;
        }
    }

    private class ComTaskInfoValueFactory implements ValueFactory<ComTaskInfo>, BpmProcessPropertyFactory {

        @Override
        public ComTaskInfo fromStringValue(String stringValue) {
            String stringValueTrimmed = stringValue.replace("[", "").replace("]", "");
            return taskService.findAllComTasks().find()
                    .stream()
                    .filter(comTask -> {
                        return stringValueTrimmed.equals("" + comTask.getId());
                    })
                    .findFirst()
                    .map(ComTaskInfo::new)
                    .orElse(null);
        }

        @Override
        public String toStringValue(ComTaskInfo object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<ComTaskInfo> getValueType() {
            return ComTaskInfo.class;
        }

        @Override
        public ComTaskInfo valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(ComTaskInfo object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, ComTaskInfo value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, ComTaskInfo value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }
}
