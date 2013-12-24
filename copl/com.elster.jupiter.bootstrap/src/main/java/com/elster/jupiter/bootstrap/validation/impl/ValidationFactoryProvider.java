package com.elster.jupiter.bootstrap.validation.impl;

import java.util.List;

import org.hibernate.validator.HibernateValidator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import com.google.common.collect.ImmutableList;

import javax.validation.Validation;
import javax.validation.ValidationProviderResolver;
import javax.validation.ValidatorFactory;
import javax.validation.spi.ValidationProvider;

@Component(name="com.elster.jupiter.bootstrap.validation.impl")
public class ValidationFactoryProvider {
	
	private ServiceRegistration<ValidatorFactory> registration;
	
	public ValidationFactoryProvider() {
		
	}
	
	@Activate
	public void activate(BundleContext context) {
		registration = context.registerService(ValidatorFactory.class, getValidatorFactory(), null);
	}
	
	@Deactivate
	public void deactivate() {
		registration.unregister();
	}
	
	private ValidatorFactory getValidatorFactory() {
		return Validation.byDefaultProvider()
	        .providerResolver( new Resolver() )
	        .configure()
	        .buildValidatorFactory();
    }
    
    static class Resolver implements ValidationProviderResolver {
        @Override
        public List<ValidationProvider<?>> getValidationProviders() {
            return ImmutableList.<ValidationProvider<?>>of(new HibernateValidator());
        }
    }
	
}
