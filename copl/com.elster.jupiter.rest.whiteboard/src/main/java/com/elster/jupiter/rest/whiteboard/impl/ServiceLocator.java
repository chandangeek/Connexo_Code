package com.elster.jupiter.rest.whiteboard.impl;

import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;

public interface ServiceLocator {

    String USERPRINCIPAL = "com.elster.jupiter.userprincipal";

    UserService getUserService();
    ThreadPrincipalService getThreadPrincipalService();
    Publisher getPublisher();
}
