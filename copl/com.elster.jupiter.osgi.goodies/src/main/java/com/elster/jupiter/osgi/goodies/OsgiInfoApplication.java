package com.elster.jupiter.osgi.goodies;

import java.util.*;
import javax.ws.rs.core.Application;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.packageadmin.PackageAdmin;
import com.elster.jupiter.orm.*;

@SuppressWarnings("deprecation")
@Component(name = "com.elster.jupiter.metering.rest" , service=Application.class , immediate = true , property = {"alias=/goodies"} )
public class OsgiInfoApplication extends Application {
	
	volatile static PackageAdmin admin;
	volatile static BundleContext context;
	volatile static OrmService ormService;
	
	private final Set<Class<?>> classes = new HashSet<>();
	
	public OsgiInfoApplication() {
		classes.add(OsgiInfoResource.class);	
		classes.add(DataModelResource.class);
	}

	public Set<Class<?>> getClasses() {
		return classes;
	}
	
	@Reference
	public void setPackageAdmin(PackageAdmin admin) {
		OsgiInfoApplication.admin = admin;
	}
	
	@Reference
	public void setOrmService(OrmService ormService) {
		OsgiInfoApplication.ormService = ormService;
	}
	
	@Activate
	public void activate(BundleContext context) {
		OsgiInfoApplication.context = context;
	}
	
	

}
