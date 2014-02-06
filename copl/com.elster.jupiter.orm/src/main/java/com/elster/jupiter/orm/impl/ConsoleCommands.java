package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.lang.reflect.Field;
import java.util.Collection;

@Component(name = "com.elster.jupiter.orm.console", service = ConsoleCommands.class, property = {"osgi.command.scope=orm", "osgi.command.function=ddl"}, immediate = true)
public class ConsoleCommands {

    private volatile BundleContext context;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
    }

    @Deactivate
    public void deactivate() {

    }

    public void ddl(String... componentName) {
        for (String component : componentName) {
            printDDL(component);
        }
    }

    private void printDDL(String componentName) {
        try {
            String filter = "(name=" + componentName + ")";
            Collection<ServiceReference<InstallService>> references = context.getServiceReferences(InstallService.class, filter);
            if (references.isEmpty()) {
                System.out.println("Nothing to do for " + componentName);
            }
            for (ServiceReference<InstallService> reference : references) {
                print(context.getService(reference));
                context.ungetService(reference);
            }
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();
        }

    }

    private void print(InstallService service) {
        try {
            Field[] declaredFields = service.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                if (DataModel.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    DataModel dataModel = (DataModel) field.get(service);
                    print(dataModel);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void print(DataModel dataModel) {
        for (Table<?> table : dataModel.getTables()) {
            for (String s : table.getDdl()) {
                System.out.println(s);
            }
        }
    }
}
