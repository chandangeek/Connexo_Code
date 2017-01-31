/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.schema.oracle.impl;

import java.util.Set;

import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.rest.util.BinderProvider;
import com.elster.jupiter.schema.oracle.OracleSchemaService;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.elster.jupiter.oracle.schema.application" , service=Application.class , immediate = true , property = {"alias=/ora"} )
public class SchemaApplication extends Application implements BinderProvider {
	
	private volatile OracleSchemaService schemaService;
	
	public SchemaApplication() {
	}
	
	public Set<Class<?>> getClasses() {
		return ImmutableSet.<Class<?>>of(SchemaResource.class);
	}
	
	@Reference
	public void setOracleSchemaService(OracleSchemaService schemaService) {
		this.schemaService = schemaService;
	}
	
	@Override
	public Binder getBinder() {
		return new AbstractBinder() {	
			@Override
			protected void configure() {
				this.bind(schemaService).to(OracleSchemaService.class);
			}
		};
	}
}
