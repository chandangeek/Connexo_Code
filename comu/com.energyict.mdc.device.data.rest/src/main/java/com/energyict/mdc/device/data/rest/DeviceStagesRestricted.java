/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import com.elster.jupiter.metering.EndDeviceStage;

import javax.ws.rs.HttpMethod;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation can be used for restriction access for a specific REST URL based on current device state.
 * Can be applied for resource class or resource method. If it is applied for both resource method and resource class
 * then values from the method annotation will be used.
 * Resource url MUST match "/some_path_segment/{device_mrid}/*" url
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
//DEZE
public @interface DeviceStagesRestricted {
    /**
     * List of restricted device states (if device in one of these states, the 404 code will be returned as a response for request)
     */
    EndDeviceStage[] value();

    /**
     * Ignored when annotation is applied to a method
     */
    String[] methods() default {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};

    String[] ignoredUserRoles() default {};
}
