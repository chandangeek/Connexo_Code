package com.elster.jupiter.servicecall.impl.example;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallHandler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

/**
 * Example handler taking care of a disconnect
 * Created by bvn on 2/18/16.
 */
@Component(name = "com.elster.jupiter.servicecall.example.disconnect.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=com.elster.jupiter.servicecall.example.disconnect.handler")
public class DisconnectHandler implements ServiceCallHandler {

    public DisconnectHandler() {
    }

    @Override
    public void onEntry(DefaultState state) {
        System.out.println("Now entering state " + state.getKey());
    }

    @Activate
    public void activate() {
        System.err.println("Activating disconnect handler");
    }

}
