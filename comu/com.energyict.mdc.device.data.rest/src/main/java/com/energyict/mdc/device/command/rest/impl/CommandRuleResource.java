package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.CommandRuleService.CommandRuleBuilder;
import com.energyict.mdc.device.command.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/commandrules")
public class CommandRuleResource {

    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final CommandRuleService commandRuleService;
    private final CommandRuleInfoFactory commandRuleInfoFactory;


    @Inject
    public CommandRuleResource(DeviceMessageSpecificationService deviceMessageSpecificationService, CommandRuleService commandRuleService, CommandRuleInfoFactory commandRuleInfoFactory) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.commandRuleService = commandRuleService;
        this.commandRuleInfoFactory = commandRuleInfoFactory;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMAND_LIMITATION_RULE,Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE})
    public Response getCommandRules(@BeanParam JsonQueryParameters queryParameters) {
        List<CommandRuleInfo> data = commandRuleService.findAllCommandRules()
                .stream()
                .map(commandRuleInfoFactory::from)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("commandrules", data,queryParameters)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE)
    public Response addCommandRule(CommandRuleInfo commandRuleInfo) {
        CommandRuleBuilder builder = commandRuleService.createRule(commandRuleInfo.name);
        builder.dayLimit(commandRuleInfo.dayLimit);
        builder.weekLimit(commandRuleInfo.weekLimit);
        builder.monthLimit(commandRuleInfo.monthLimit);

        commandRuleInfo.commands.stream()
                .forEach(commandInfo -> builder.command(commandInfo.commandName));

        builder.add();
        return Response.ok().build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed({Privileges.Constants.VIEW_COMMAND_LIMITATION_RULE,Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE})
    public CommandRuleInfo getCommandRule(@PathParam("id") long id) {
        CommandRule commandRule = commandRuleService.findCommandRule(id).orElseThrow(() -> new IllegalArgumentException("No command rule with given id"));
        return commandRuleInfoFactory.createWithChanges(commandRule);
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE)
    public Response changeCommandRule(@PathParam("id") long id, CommandRuleInfo commandRuleInfo) {
        CommandRule commandRule = commandRuleService.findCommandRule(id).orElseThrow(() -> new IllegalArgumentException("No command rule with given id"));
        if(!commandRule.isActive() && commandRuleInfo.active) {
            commandRule.activate();
        } else if (commandRule.isActive() && !commandRuleInfo.active) {
            commandRule.deactivate();
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE)
    public Response deleteCommandRule(@PathParam("id") long id) {
        CommandRule commandRule = commandRuleService.findCommandRule(id).orElseThrow(() -> new IllegalArgumentException("No command rule with given id"));
        commandRuleService.deleteRule(commandRule);
        return Response.ok().build();
    }

    @POST
    @Path("/{id}/accept")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE)
    public Response acceptChanges(@PathParam("id") long id) {
        CommandRule commandRule = commandRuleService.findCommandRule(id).orElseThrow(() -> new IllegalArgumentException("No command rule with given id"));
        commandRule.approve();
        return Response.ok().build();
    }


    @POST
    @Path("/{id}/reject")
    @Transactional
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE)
    public Response rejectChanges(@PathParam("id") long id) {
        CommandRule commandRule = commandRuleService.findCommandRule(id).orElseThrow(() -> new IllegalArgumentException("No command rule with given id"));
        commandRule.reject();
        return Response.ok().build();
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE)
    public Response getCategories() {
        List<IdWithNameInfo> categories = this.deviceMessageSpecificationService.filteredCategoriesForUserSelection()
                .stream()
                .map(deviceMessageCategory -> new IdWithNameInfo(deviceMessageCategory.getId(), deviceMessageCategory.getName()))
                .sorted((o1, o2) -> o1.name.compareTo(o2.name))
                .collect(Collectors.toList());

        return Response.ok(categories).build();
    }

    @GET
    @Path("/commands")
    @RolesAllowed(Privileges.Constants.ADMINISTRATE_COMMAND_LIMITATION_RULE)
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getCommands(@BeanParam JsonQueryFilter jsonQueryFilter) {
        List<String> alreadySelectedCommands = jsonQueryFilter.getStringList("selectedcommands");
        List<Integer> selectedCategories = jsonQueryFilter.getIntegerList("categories");

        List<CommandInfo> commands = this.deviceMessageSpecificationService.filteredCategoriesForUserSelection()
                .stream()
                .filter(deviceMessageCategory -> selectedCategories.size() == 0 || selectedCategories.contains(deviceMessageCategory.getId()))
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                .map(DeviceMessageCategory::getMessageSpecifications)
                .flatMap(List::stream)
                .filter(deviceMessageSpec -> !alreadySelectedCommands.contains(deviceMessageSpec.getId().name()))
                .map(deviceMessageSpec -> new CommandInfo(deviceMessageSpec.getCategory().getName(), deviceMessageSpec.getName(), deviceMessageSpec.getId().name()))
                .sorted(CommandInfo::compareTo)
                .collect(Collectors.toList());

        return Response.ok(commands).build();
    }



}

