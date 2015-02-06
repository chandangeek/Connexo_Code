package com.elster.jupiter.yellowfin.impl;

import com.elster.jupiter.events.LocalEvent;
import com.elster.jupiter.pubsub.EventHandler;
import com.elster.jupiter.pubsub.Subscriber;
import com.elster.jupiter.yellowfin.YellowfinService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.yellowfin.yellowfineventhandler", service = Subscriber.class, immediate = true)
public class YellowfinEventHandler extends EventHandler<LocalEvent> {

    private static final String LOGOUTTOPIC = "com/elster/jupiter/http/LOGOUT";

    private volatile YellowfinService yellowfinService;

    public YellowfinEventHandler() {
        super(LocalEvent.class);
    }

    @Reference
    public void setYellowfinService(YellowfinService yellowfinService) {
        this.yellowfinService = yellowfinService;
    }

    @Override
    protected void onEvent(LocalEvent event, Object... eventDetails) {
        if (event.getType().getTopic().equals(LOGOUTTOPIC)) {
            String user = (String) event.getSource();
            doLogout(user);
        }
    }

    private void doLogout(String user){
        this.yellowfinService.logout(user);
    }
}
