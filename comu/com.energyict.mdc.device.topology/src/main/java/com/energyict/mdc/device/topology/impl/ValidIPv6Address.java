/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.device.topology.G3DeviceAddressInformation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that the IPv6 address of a {@link G3DeviceAddressInformation} is valid.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-17 (10:57)
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { ValidIPv6AddressValidator.class })
public @interface ValidIPv6Address {

    String message() default MessageSeeds.Keys.INVALID_IPV6_ADDRESS;

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}