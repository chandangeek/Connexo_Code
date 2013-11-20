package com.elster.jupiter.osgi.goodies;

import com.elster.jupiter.orm.OrmService;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.osgi.service.packageadmin.PackageAdmin;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
@Component(name = "com.elster.jupiter.osgi.goodies" , service=Application.class , immediate = true , property = {"alias=/goodies"} )
public class OsgiInfoApplication extends Application {
	
	static volatile PackageAdmin admin;
	static volatile BundleContext context;
	static volatile OrmService ormService;
	
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
	
	@Deactivate
	public void deactivate() {
		OsgiInfoApplication.ormService = null;
		OsgiInfoApplication.context = null;
	}
	
}
