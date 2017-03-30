/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Validates the {@link ValidIPv6Address} constraint against a {@link G3DeviceAddressInformationImpl}.
 * When valid, writes the Inet6Address into the G3DeviceAddressInformationImpl.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-17 (11:00)
 */
public class ValidIPv6AddressValidator implements ConstraintValidator<ValidIPv6Address, G3DeviceAddressInformationImpl> {

    @Override
    public void initialize(ValidIPv6Address constraintAnnotation) {
        // No need to hold onto the annotation
    }

    @Override
    public boolean isValid(G3DeviceAddressInformationImpl addressInformation, ConstraintValidatorContext context) {
        try {
            String stringAddress = addressInformation.getIpv6StringAddress();
            if (this.notEmpty(stringAddress)) {
                InetAddress inetAddress = Inet6Address.getByName(stringAddress);
                if (inetAddress instanceof Inet6Address) {
                    addressInformation.setIpv6Address(inetAddress);
                    return true;
                }
                else {
                    return false;
                }
            }
            else {
                return true;    // Other annotations are validating absence of information
            }
        }
        catch (UnknownHostException e) {
            return false;
        }
    }

    private boolean notEmpty(String stringAddress) {
        return stringAddress != null && !stringAddress.isEmpty();
    }

}