package com.elster.jupiter.calendar.impl;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.calendar.EventType;
import com.elster.jupiter.calendar.security.Privileges;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.FullInstaller;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class InstallerImpl implements FullInstaller, PrivilegesProvider {

    private final UserService userService;
    private final EventService eventService;
    private final DataModel dataModel;

    @Inject
    public InstallerImpl(UserService userService, EventService eventService, DataModel dataModel) {
        this.userService = userService;
        this.eventService = eventService;
        this.dataModel = dataModel;
    }

    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        dataModelUpgrader.upgrade(dataModel, Version.latest());
        doTry(
                "Create default Calendar categories.",
                this::createTOUCategory,
                logger
        );
        doTry(
                "Create event types for CAL.",
                this::createEventTypes,
                logger
        );
        userService.addModulePrivileges(this);
    }

    private void createTOUCategory() {
        CategoryImpl category = this.dataModel.getInstance(CategoryImpl.class);
        category.init(CalendarServiceImpl.TIME_OF_USE_CATEGORY_NAME);
        category.save();
    }

    private void createEventTypes() {
        for (EventType eventType : EventType.values()) {
            eventType.install(eventService);
        }
    }

    @Override
    public String getModuleName() {
        return CalendarService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(getModuleName(),
                Privileges.RESOURCE_TOU_CALENDARS.getKey(), Privileges.RESOURCE_TOU_CALENDARS_DESCRIPTION.getKey(),
                Arrays.asList(Privileges.Constants.MANAGE_TOU_CALENDARS)));
        return resources;
    }


}
