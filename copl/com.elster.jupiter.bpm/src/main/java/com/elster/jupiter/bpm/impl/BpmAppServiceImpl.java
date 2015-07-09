package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmAppService;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.http.whiteboard.App;
import com.elster.jupiter.license.License;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.UserService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(
        name = "com.elster.jupiter.bpm.app",
        service = {BpmAppService.class},
        immediate = true
)
public class BpmAppServiceImpl implements BpmAppService {

    private volatile ServiceRegistration<App> registration;
    private volatile BpmService bpmService;
    private volatile License license;

    public BpmAppServiceImpl() {
    }

    @Inject
    public BpmAppServiceImpl(BpmService bpmService, BundleContext context) {
        setBpmService(bpmService);
        activate(context);
    }

    @Activate
    public final void activate(BundleContext context) {
        App app = new App("BPM", "Flow", "connexo", bpmService.getBpmServer().getUrl(), user -> user.getPrivileges(bpmService.COMPONENTNAME).stream().anyMatch(p -> "privilege.design.bpm".equals(p.getName())));
        registration = context.registerService(App.class, app, null);
    }

    @Deactivate
    public void stop(BundleContext context) throws Exception {
        registration.unregister();
    }

    @Reference
    public void setBpmService(BpmService bpmService) {
        this.bpmService = bpmService;
    }

    @Reference(target = "(com.elster.jupiter.license.application.key=" + APPLICATION_KEY + ")")
    public void setLicense(License license) {
        this.license = license;
    }
}
