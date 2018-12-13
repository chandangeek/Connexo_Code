/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.customtask;


import com.elster.jupiter.users.User;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ConsumerType;

import javax.validation.ConstraintValidatorContext;
import java.util.List;

@ConsumerType
public interface CustomTaskFactory extends HasName {

    boolean isValid(List<CustomTaskProperty> properties, ConstraintValidatorContext context);

    String getDisplayName();

    List<String> targetApplications();

    List<PropertiesInfo> getProperties();

    List<CustomTaskAction> getActionsForUser(User user, String application);
}
