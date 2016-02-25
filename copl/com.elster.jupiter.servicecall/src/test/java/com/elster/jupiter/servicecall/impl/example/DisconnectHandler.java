package com.elster.jupiter.servicecall.impl.example;

import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.impl.ServiceCallServiceImpl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import javax.inject.Inject;
import java.util.HashMap;

/**
 * Example handler taking care of a disconnect
 * Created by bvn on 2/18/16.
 */
@Component(name = "com.elster.jupiter.servicecall.example.disconnect.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=DisconnectHandler1")
public class DisconnectHandler implements ServiceCallHandler {

    public DisconnectHandler() {
    }

    @Inject
    public DisconnectHandler(ServiceCallServiceImpl serviceCallService) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name", "DisconnectHandler1");
        serviceCallService.addServiceCallHandler(this, map);
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
