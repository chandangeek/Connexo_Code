package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.JsonQueryFilter;
import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;
import com.elster.jupiter.rest.util.Transactional;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.CommandRuleService.CommandRuleBuilder;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Path("/commandrules")
public class CommandRuleResource {

    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final CommandRuleService commandRuleService;


    @Inject
    public CommandRuleResource(DeviceMessageSpecificationService deviceMessageSpecificationService, CommandRuleService commandRuleService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.commandRuleService = commandRuleService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getCommandRules(@BeanParam JsonQueryParameters queryParameters) {
        List<CommandRuleInfo> data = commandRuleService.findAllCommandRules()
                .stream()
                .map(CommandRuleInfo::from)
                .collect(Collectors.toList());
        return Response.ok(PagedInfoList.fromCompleteList("commandrules", data,queryParameters)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    @Transactional
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
    public CommandRuleInfo getCommandRule(@PathParam("id") long id) {
        CommandRule commandRule = commandRuleService.findCommandRule(id).orElseThrow(() -> new IllegalArgumentException("No command rule with given id"));
        return CommandRuleInfo.from(commandRule);
    }

    @GET
    @Path("/categories")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getCategories() {
        List<IdWithNameInfo> categories = this.deviceMessageSpecificationService.filteredCategoriesForUserSelection()
                .stream()
                .map(deviceMessageCategory -> new IdWithNameInfo(deviceMessageCategory.getId(), deviceMessageCategory.getName()))
                .collect(Collectors.toList());

        return Response.ok(categories).build();
    }

    @GET
    @Path("/commands")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getCommands(@BeanParam JsonQueryFilter jsonQueryFilter) {
        List<String> alreadySelectedCommands = jsonQueryFilter.getStringList("selectedcommands");
        List<Integer> selectedCategories = jsonQueryFilter.getIntegerList("categories");

        List<CommandInfo> commands = this.deviceMessageSpecificationService.filteredCategoriesForUserSelection()
                .stream()
                .filter(deviceMessageCategory -> selectedCategories.size() == 0 || selectedCategories.contains(deviceMessageCategory.getId()))
                .map(DeviceMessageCategory::getMessageSpecifications)
                .flatMap(List::stream)
                .filter(deviceMessageSpec -> !alreadySelectedCommands.contains(deviceMessageSpec.getId().name()))
                .map(deviceMessageSpec -> new CommandInfo(deviceMessageSpec.getCategory().getName(), deviceMessageSpec.getName(), deviceMessageSpec.getId().name()))
                .collect(Collectors.toList());

        return Response.ok(commands).build();
    }

}

