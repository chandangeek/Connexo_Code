package com.elster.jupiter.calendar.importers.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.UserService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.time.Clock;

@Component(name = "com.elster.jupiter.calendar.importers.importers.CalendarImporterContext", service = {CalendarImporterContext.class})
public class CalendarImporterContext {
    private volatile Thesaurus thesaurus;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;

    public CalendarImporterContext() {
    }

    @Inject
    public CalendarImporterContext(NlsService nlsService,
                                     UserService userService,
                                     ThreadPrincipalService threadPrincipalService,
                                     Clock clock) {
        setNlsService(nlsService);
        setUserService(userService);
        setThreadPrincipalService(threadPrincipalService);
        setClock(clock);
    }



    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(CalendarImporterMessageHandler.COMPONENT, Layer.DOMAIN);
    }


    @Reference
    public final void setUserService(UserService userService) {
        this.userService = userService;
    }

    public UserService getUserService() {
        return userService;
    }

    @Reference
    public final void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public ThreadPrincipalService getThreadPrincipalService() {
        return threadPrincipalService;
    }

    public Clock getClock() {
        return clock;
    }

    @Reference
    public final void setClock(Clock clock) {
        this.clock = clock;
    }
}

