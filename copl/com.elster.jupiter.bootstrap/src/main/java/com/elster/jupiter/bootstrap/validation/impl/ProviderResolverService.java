/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.validation.impl;

import java.util.List;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;

import org.hibernate.validator.HibernateValidator;
import org.osgi.service.component.annotations.Component;

import com.google.common.collect.ImmutableList;

@Component(name="com.elster.jupiter.bootstrap.validation")
public class ProviderResolverService implements ValidationProviderResolver {
	
	public ProviderResolverService() {	
	}
	
	public List<ValidationProvider<?>> getValidationProviders() {
		return ImmutableList.<ValidationProvider<?>>of(new HibernateValidator());
    }	
}
