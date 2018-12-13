/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.h2.impl;

import com.google.common.collect.ImmutableList;
import org.hibernate.validator.HibernateValidator;
import org.osgi.service.component.annotations.Component;

import javax.validation.ValidationProviderResolver;
import javax.validation.spi.ValidationProvider;
import java.util.List;

@Component(name="com.elster.jupiter.bootstrap.validation")
public class ProviderResolverService implements ValidationProviderResolver {
	
	public ProviderResolverService() {	
	}
	
	public List<ValidationProvider<?>> getValidationProviders() {
		return ImmutableList.<ValidationProvider<?>>of(new HibernateValidator());
    }	
}
