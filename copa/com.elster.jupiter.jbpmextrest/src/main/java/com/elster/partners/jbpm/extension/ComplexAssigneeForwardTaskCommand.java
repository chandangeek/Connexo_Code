/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;


import org.drools.core.util.StringUtils;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.commands.UserGroupCallbackTaskCommand;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.User;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.kie.internal.task.api.model.InternalTask;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(
        name = "complexassignee-forward-task-command"
)
@XmlAccessorType(XmlAccessType.NONE)
public class ComplexAssigneeForwardTaskCommand extends UserGroupCallbackTaskCommand<Void> {
    private static final long serialVersionUID = 1L;

    private String groupId;

    public ComplexAssigneeForwardTaskCommand() {
    }

    public ComplexAssigneeForwardTaskCommand(long taskId, String groupId) {
        this.taskId = taskId;
        this.groupId = groupId;
    }

    public Void execute(Context cntxt) {
        TaskContext context = (TaskContext)cntxt;
        PeopleAssignments peopleAssignments = context.getPersistenceContext()
                .findTask(taskId)
                .getPeopleAssignments();

        if(!groupId.equals("Unassigned")) {
            if (peopleAssignments.getPotentialOwners()
                    .stream()
                    .filter(org -> org instanceof Group)
                    .noneMatch(g -> g.getId().equals(groupId))) {
                Group group = createGroup(groupId, context);
                List<OrganizationalEntity> potentialGroupOwner = peopleAssignments.getPotentialOwners()
                        .stream()
                        .filter(org -> org instanceof User)
                        .collect(Collectors.toList());
                peopleAssignments.getPotentialOwners().clear();
                peopleAssignments.getPotentialOwners().addAll(potentialGroupOwner);
                peopleAssignments.getPotentialOwners().add(group);
            }
            InternalTask intTask = (InternalTask) context.getPersistenceContext().findTask(taskId);
            intTask.setPeopleAssignments(peopleAssignments);
            context.getPersistenceContext().persistTask(intTask);
        }else {
            List<OrganizationalEntity> potentialGroupOwner = peopleAssignments.getPotentialOwners()
                    .stream()
                    .filter(org -> org instanceof User)
                    .collect(Collectors.toList());
            peopleAssignments.getPotentialOwners().clear();
            peopleAssignments.getPotentialOwners().addAll(potentialGroupOwner);
            InternalTask intTask = (InternalTask) context.getPersistenceContext().findTask(taskId);
            intTask.setPeopleAssignments(peopleAssignments);
            context.getPersistenceContext().persistTask(intTask);
        }

        return null;
    }

    private Group createGroup(String groupId,TaskContext context ){
        Group group = context.getPersistenceContext().findGroup(groupId);
        boolean groupExists = group != null;
        if(!StringUtils.isEmpty(groupId) && !groupExists) {
            group = TaskModelProvider.getFactory().newGroup();
            ((InternalOrganizationalEntity)group).setId(groupId);
            this.persistIfNotExists(group, context);
        }
        return group;
    }
}
