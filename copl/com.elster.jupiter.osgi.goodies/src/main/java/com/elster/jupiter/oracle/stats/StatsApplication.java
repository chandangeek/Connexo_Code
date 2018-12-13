/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.oracle.stats;

import java.util.Set;

import javax.sql.DataSource;
import javax.ws.rs.core.Application;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elster.jupiter.rest.util.BinderProvider;
import com.google.common.collect.ImmutableSet;

@Component(service=Application.class, property={"alias=/stats"})
public class StatsApplication extends Application implements BinderProvider {
	
	private volatile StatService statService;
	private volatile DataSource dataSource;
	
	@Reference
	public void setStatService(StatService statService) {
		this.statService = statService;
	}

	@Reference
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public Set<Class<?>> getClasses() {
		return ImmutableSet.<Class<?>>of(StatsResource.class);
	}

	@Override
	public Binder getBinder() {
		return new AbstractBinder() {			
			@Override
			protected void configure() {
				this.bind(statService).to(StatService.class);
				this.bind(dataSource).to(DataSource.class);
			}
		};
	}
}
