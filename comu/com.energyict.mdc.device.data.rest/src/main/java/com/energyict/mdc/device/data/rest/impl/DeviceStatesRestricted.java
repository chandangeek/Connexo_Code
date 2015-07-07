package com.energyict.mdc.device.data.rest.impl;

import com.energyict.mdc.device.lifecycle.config.DefaultState;

import javax.ws.rs.HttpMethod;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation can be used for restriction access for a specific REST URL based on current device state.
 * Resource url MUST match "devices/{device_MRID}*" url
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface DeviceStatesRestricted {
    /**
     * List of restricted device states (if device in one of these state, the 404 code will be returned as a response for request)
     */
    DefaultState[] value();

    /**
     * Ignored when annotation is applied to method
     */
    String[] methods() default {HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE};
}
