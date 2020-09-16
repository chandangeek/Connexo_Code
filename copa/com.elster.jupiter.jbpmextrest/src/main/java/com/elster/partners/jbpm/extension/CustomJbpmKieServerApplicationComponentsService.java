package com.elster.partners.jbpm.extension;

import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.RuntimeDataService;
import org.kie.api.task.TaskService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.RulesExecutionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CustomJbpmKieServerApplicationComponentsService implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = "jBPM";

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
        // Do not accept calls from extensions other than the owner extension:
        if ( !OWNER_EXTENSION.equals(extension) ) {
            return Collections.emptyList();
        }

        KieServerRegistry context = null;
        RuntimeDataService runtimeDataService=null;
        TaskService taskService=null;
        FormManagerService formManagerService=null;

        System.out.println("****************************");
        System.out.println("****************************");
        System.out.println("****************************");
        for( Object object : services ) {
            System.out.println(object.getClass() + object.toString());
            if( KieServerRegistry.class.isAssignableFrom(object.getClass()) ) {
                context = (KieServerRegistry) object;
                continue;
            }
            if( RuntimeDataService.class.isAssignableFrom(object.getClass()) ) {
                runtimeDataService = (RuntimeDataService) object;
                continue;
            }
            if( FormManagerService.class.isAssignableFrom(object.getClass()) ) {
                formManagerService= (FormManagerService) object;
                continue;
            }
        }

        List<Object> components = new ArrayList<Object>(1);
        if( SupportedTransports.REST.equals(type) ) {
            components.add(new CustomJbpmResource(context,runtimeDataService,formManagerService));
        }

        return components;
    }
}
