/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.demo.customtask.impl;

import com.elster.jupiter.customtask.CustomTaskAction;
import com.elster.jupiter.customtask.CustomTaskFactory;
import com.elster.jupiter.customtask.CustomTaskProperty;
import com.elster.jupiter.customtask.PropertiesInfo;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.InvalidValueException;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.energyict.mdc.demo.customtask.security.Privileges;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.ConstraintValidatorContext;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component(
        name = "com.energyict.mdc.demo.customtask.impl.DemoCustomTaskFactory",
        service = CustomTaskFactory.class,
        property = "name=" + MessageSeeds.COMPONENT_NAME,
        immediate = true)
public class DemoCustomTaskFactory implements CustomTaskFactory {

    public static final String NAME = "MDCDemoCustomTask";
    public static final String SUBSCRIBER_NAME = "MDCDemoCustomTask";
    public static final String DESTINATION_NAME = "MDCDemoCustomTask";
    static final String SUBSCRIBER_DISPLAY_NAME = "Handle MDC custom task";
    static final String DISPLAY_NAME = "Demo custom task for MDC";

    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile MeteringGroupsService meteringGroupsService;

    public DemoCustomTaskFactory() {
    }

    @Inject
    public DemoCustomTaskFactory(NlsService nlsService, PropertySpecService propertySpecService, MeteringGroupsService meteringGroupsService) {
        this();
        setPropertySpecService(propertySpecService);
        setThesaurus(nlsService);
        setMeteringGroupsService(meteringGroupsService);
    }

    @Reference
    public void setThesaurus(NlsService nlsService) {
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

    @Override
    public boolean isValid(List<CustomTaskProperty> properties, ConstraintValidatorContext context) {
        boolean valid = true;
        List<PropertySpec> propertySpecs = Arrays.asList(getDataSelectorProperty(), getCountProperty(), getDataSeparatorProperty());
        for (PropertySpec propertySpec : propertySpecs) {
            try {
                Object value = properties.stream()
                        .filter(customTaskProperty -> customTaskProperty.getName().compareToIgnoreCase(propertySpec.getName()) == 0)
                        .findFirst().map(customTaskProperty -> customTaskProperty.getValue()).orElse(null);

                propertySpec.validateValue(value);
            } catch (InvalidValueException e) {
                valid = false;
                context.buildConstraintViolationWithTemplate(MessageFormat.format(e.getDefaultPattern(), e.getArguments()))
                        .addPropertyNode("properties." + propertySpec.getName()).addConstraintViolation()
                        .disableDefaultConstraintViolation();
            }

        }
        return valid;
    }

    @Override
    public List<PropertiesInfo> getProperties() {
        return Arrays.asList(
                new PropertiesInfo("dataselection", "Data selector", Arrays.asList(
                        getDataSelectorProperty(), getCountProperty())),
                new PropertiesInfo("dataseparator", "Data separator", Arrays.asList(
                        getDataSeparatorProperty()))
        );
    }

    @Override
    public List<CustomTaskAction> getActionsForUser(User user, String application) {
        List<CustomTaskAction> customTaskActions = new ArrayList<>();

        user.getPrivileges(application).stream()
                .filter(privilege -> privilege.getName().compareToIgnoreCase(Privileges.Constants.ADMINISTRATE_MDC_DEMO_CUSTOM_TASK) == 0)
                .forEach(privilege -> customTaskActions.addAll(Arrays.asList(CustomTaskAction.ADMINISTRATE, CustomTaskAction.EDIT)));

        user.getPrivileges(application).stream()
                .filter(privilege -> privilege.getName().compareToIgnoreCase(Privileges.Constants.VIEW_MDC_DEMO_CUSTOM_TASK) == 0)
                .forEach(p -> customTaskActions.addAll(Arrays.asList(CustomTaskAction.VIEW, CustomTaskAction.VIEW_HISTORY, CustomTaskAction.VIEW_HISTORY_LOG)));

        user.getPrivileges(application).stream()
                .filter(privilege -> privilege.getName().compareToIgnoreCase(Privileges.Constants.RUN_MDC_DEMO_CUSTOM_TASK) == 0)
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

    private PropertySpec getDataSelectorProperty() {
       List<String> allGroups = meteringGroupsService.getEndDeviceGroupQuery()
                .select(Condition.TRUE, Order.ascending("upper(name)"))
                .stream()
                .map(endDeviceGroup -> new String(endDeviceGroup.getName()))
                .collect(Collectors.toList());

        return propertySpecService
                .specForValuesOf(new StringFactory())
                .named(TranslationKeys.SELECTOR)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .addValues(allGroups)
                .setDefaultValue(allGroups.size() == 1 ? allGroups.get(0) : null)
                .markExhaustive(PropertySelectionMode.COMBOBOX)
                .finish();
    }

    private PropertySpec getCountProperty() {
        return propertySpecService
                .longSpec()
                .named(TranslationKeys.COUNT)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .setDefaultValue(5L)
                .finish();
    }

    private PropertySpec getDataSeparatorProperty() {
        return propertySpecService
                .stringSpec()
                .named(TranslationKeys.SEPARATOR)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(";")
                .markRequired()
                .finish();
    }
}
