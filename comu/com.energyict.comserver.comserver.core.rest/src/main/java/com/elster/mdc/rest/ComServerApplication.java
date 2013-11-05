package com.elster.mdc.rest;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.ws.rs.core.Application;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.mdc.rest" , service=Application.class , immediate = true , property = {"alias=/comserver"} )
public class ComServerApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return ImmutableSet.<Class<?>>of(ComServerRest.class);
    }


}
