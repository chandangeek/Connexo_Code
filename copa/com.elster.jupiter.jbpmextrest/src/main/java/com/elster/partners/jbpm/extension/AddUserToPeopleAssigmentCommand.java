/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import org.drools.core.util.StringUtils;
import org.jbpm.services.task.commands.TaskContext;
import org.jbpm.services.task.commands.UserGroupCallbackTaskCommand;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.User;
import org.kie.internal.command.Context;
import org.kie.internal.task.api.TaskModelProvider;
import org.kie.internal.task.api.model.InternalOrganizationalEntity;
import org.kie.internal.task.api.model.InternalTask;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(
        name = "adduser-peopleassigment-task-command"
)
@XmlAccessorType(XmlAccessType.NONE)
public class AddUserToPeopleAssigmentCommand extends UserGroupCallbackTaskCommand<Void> {
    private static final long serialVersionUID = 2L;

    public AddUserToPeopleAssigmentCommand() {
    }

    public AddUserToPeopleAssigmentCommand(long taskId, String userId) {
        this.taskId = taskId;
        this.userId = userId;
    }

    public Void execute(Context cntxt) {
        TaskContext context = (TaskContext)cntxt;
        PeopleAssignments peopleAssignments = context.getPersistenceContext()
                .findTask(taskId)
                .getPeopleAssignments();
        if (peopleAssignments.getPotentialOwners()
                .stream()
                .filter(org -> org instanceof User)
                .noneMatch(g -> g.getId().equals(userId))) {

            User user = createOrGetUser(this.userId, context);
            peopleAssignments.getPotentialOwners().add(user);
            InternalTask intTask = (InternalTask) context.getPersistenceContext().findTask(taskId);
            intTask.setPeopleAssignments(peopleAssignments);
            context.getPersistenceContext().persistTask(intTask);
        }
        return null;
    }

    private User createOrGetUser(String userId, TaskContext context ){
        User user = context.getPersistenceContext().findUser(userId);
        boolean userExist = user != null;
        if(!StringUtils.isEmpty(userId) && !userExist) {
            user = TaskModelProvider.getFactory().newUser();
            ((InternalOrganizationalEntity)user).setId(userId);
            this.persistIfNotExists(user, context);
        }
        return user;
    }
}
