package com.elster.jupiter.osgi.goodies;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.rest.util.BinderProvider;
import com.google.common.collect.ImmutableSet;

import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.packageadmin.PackageAdmin;

import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
@Component(name = "com.elster.jupiter.osgi.goodies.application" , service=Application.class , immediate = true , property = {"alias=/goodies"} )
public class OsgiInfoApplication extends Application implements BinderProvider {
	
	volatile PackageAdmin admin;
	volatile BundleContext context;
	volatile OrmService ormService;
	
	private final Set<Class<?>> classes = new HashSet<>();
	
	public OsgiInfoApplication() {
	}

	public Set<Class<?>> getClasses() {
		return ImmutableSet.of(OsgiInfoResource.class,DataModelResource.class);
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
	}

	@Override
	public Binder getBinder() {
		return new AbstractBinder() {
			@Override
			protected void configure() {
				this.bind(admin).to(PackageAdmin.class);
				this.bind(context).to(BundleContext.class);
				this.bind(ormService).to(OrmService.class);
			}
		};
	}
	
}
