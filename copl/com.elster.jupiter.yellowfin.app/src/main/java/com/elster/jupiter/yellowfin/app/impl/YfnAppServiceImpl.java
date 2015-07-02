package com.elster.jupiter.yellowfin.app.impl;

import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.http.whiteboard.BundleResolver;
import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.license.License;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.yellowfin.app.YfnAppService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/01/2015
 * Time: 12:32
 */
@Component(
        name = "com.elster.jupiter.yellowfin.app",
        service = {YfnAppService.class,ApplicationPrivilegesProvider.class},
        immediate = true
)
public class YfnAppServiceImpl implements YfnAppService, ApplicationPrivilegesProvider{

    public static final String APP_NAME = "Facts";
    public static final String APP_ICON = "connexo";

    public static final String HTTP_RESOURCE_ALIAS = "/facts";
    public static final String HTTP_RESOURCE_LOCAL_NAME = "/js/yellowfin";

    private static final String YELLOWFIN_URL = "com.elster.jupiter.yellowfin.url";
    private static final String DEFAULT_YELLOWFIN_URL = "http://localhost:8081";

    private volatile ServiceRegistration<App> registration;
    private volatile License license;

    public YfnAppServiceImpl() {
    }

    @Inject
    public YfnAppServiceImpl(BundleContext context) {
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        String url = context.getProperty(YELLOWFIN_URL);
        if (url == null || !url.startsWith("http://")) {
            url = DEFAULT_YELLOWFIN_URL;
        }
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, HTTP_RESOURCE_LOCAL_NAME, new BundleResolver(context), new DefaultStartPage(APP_NAME));
        App app = new App(YfnAppService.APPLICATION_KEY, APP_NAME, APP_ICON, HTTP_RESOURCE_ALIAS, resource, url, user -> user.getPrivileges("YFN").stream().anyMatch(p -> "privilege.design.reports".equals(p.getName())));

        registration = context.registerService(App.class, app, null);
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }


    @Reference(target = "(com.elster.jupiter.license.application.key=" + YfnAppService.APPLICATION_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }


    @Override
    public List<String> getApplicationPrivileges() {
        return Arrays.asList("privilege.design.reports");
    }

    @Override
    public String getApplicationName() {
        return YfnAppService.APPLICATION_KEY;
    }
}

