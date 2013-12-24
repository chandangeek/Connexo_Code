package com.elster.jupiter.bootstrap.validation.impl;

import org.hibernate.validator.HibernateValidator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

@Component(name="com.elster.jupiter.bootstrap.validation.impl")
public class ValidationFactoryProvider {
	
	ServiceRegistration<ValidatorFactory> registration;
	
	@Activate
	public void activate(BundleContext context) {
		registration = context.registerService(ValidatorFactory.class, getValidatorFactory(), null);
	}
	
	@Deactivate
	public void deactivate() {
		registration.unregister();
	}
	
	private ValidatorFactory getValidatorFactory() {
		return Validation.byProvider( HibernateValidator.class ).configure().buildValidatorFactory();		
	}

	
}
