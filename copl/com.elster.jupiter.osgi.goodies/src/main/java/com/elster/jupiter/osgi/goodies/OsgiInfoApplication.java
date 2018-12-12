/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.*;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.packageadmin.PackageAdmin;

import javax.ws.rs.core.Application;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

@SuppressWarnings("deprecation")
@Component(name = "com.elster.jupiter.osgi.goodies.application" , service=Application.class , immediate = true , property = {"alias=/goodies"} )
public class OsgiInfoApplication extends Application implements BinderProvider {
	
	private volatile PackageAdmin admin;
	private volatile BundleContext context;
	private volatile OrmService ormService;
	private final Events events = new Events();
	private volatile ServiceRegistration<EventHandler> eventRegistration;
	private volatile ServiceRegistration<HttpResource> httpRegistration;
	
	public OsgiInfoApplication() {
	}

	public Set<Class<?>> getClasses() {
		return ImmutableSet.of(OsgiInfoResource.class,DataModelResource.class,EventResource.class);
	}
	
	@Reference
	public void setPackageAdmin(PackageAdmin admin) {
		this.admin = admin;
	}
	
	@Reference
	public void setOrmService(OrmService ormService) {
		this.ormService = ormService;
	}
	
	@Activate
	public void activate(BundleContext context) {
		this.context = context;
		Dictionary<String,String> dict = new Hashtable<>();
		dict.put(EventConstants.EVENT_TOPIC,"*");
		eventRegistration = context.registerService(EventHandler.class, events,dict);
		HttpResource resource = new HttpResource("/goodies", "/js" , new BundleResolver(context));
		httpRegistration = context.registerService(HttpResource.class, resource,null);
	}
	
	@Deactivate
	public void deactivate() {
		eventRegistration.unregister();
		httpRegistration.unregister();
	}

	@Override
	public Binder getBinder() {
		return new AbstractBinder() {
			@Override
			protected void configure() {
				this.bind(admin).to(PackageAdmin.class);
				this.bind(context).to(BundleContext.class);
				this.bind(ormService).to(OrmService.class);
				this.bind(events).to(Events.class);
			}
		};
	}
	
}
