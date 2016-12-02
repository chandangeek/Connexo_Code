package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.rest.util.JsonQueryParameters;
import com.elster.jupiter.rest.util.PagedInfoList;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/commandrules")
public class CommandRuleResource {


    @Inject
    public CommandRuleResource() {

    }

    @GET
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public Response getCommandRules(@BeanParam JsonQueryParameters queryParameters) {
        List<CommandRuleInfo> data =  createDummyData(19);
        return Response.ok(PagedInfoList.fromCompleteList("commandrules", data,queryParameters)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public CommandRuleInfo getCommandRule(@PathParam("id") long id) {
        return createDummyData(Math.toIntExact(id) + 1).get(Math.toIntExact(id));
    }

    private List<CommandRuleInfo> createDummyData(int size) {
        List<CommandRuleInfo> infos = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            CommandRuleInfo info = new CommandRuleInfo();
            info.id = i;
            info.name = "Rule " + i;
            if(i%2 == 1) {
                info.dayLimit = i * 2;
            }
            info.weekLimit = i * 3;
            if(i%2==0) {
                info.monthLimit = i * 5;
            }
            info.active = i%4 != 0;
            if(i%3 == 0) {
                info.statusMessage = "Pending changes";
            }
            info.version = 1;
            for(int j = 0; j < 2; j++) {
                if(i%2 == 0) {
                    info.commands.add(new CommandInfo("Category " + i, "Command " + j));
                    info.commands.add(new CommandInfo("Category " + i, "Command " + i));
                }
                int sum = i+j;
                info.commands.add(new CommandInfo("Category " + j, "Command " + sum));
            }
            infos.add(info);
        }
        return infos;
    }

}

