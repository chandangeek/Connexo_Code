package com.energyict.mdc.engine.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.cbo.DateTimeFormatGenerator;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.Formatter;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.security.Principal;
import java.time.Instant;

/**
 * Provides an implementation for the {@link Formatter} interface
 * that uses the {@link User}'s date and time preferences.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (16:49)
 */
@Component(name = "com.energyict.mdc.upl.messages.legacy.date.formatter", service = {Formatter.class}, immediate = true)
@SuppressWarnings("unused")
public class FormatterImpl implements Formatter {

    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile UserService userService;

    // For OSGi purposes
    public FormatterImpl() {
        super();
    }

    // For testing purposes
    @Inject
    public FormatterImpl(ThreadPrincipalService threadPrincipalService, UserService userService) {
        this();
        this.setThreadPrincipalService(threadPrincipalService);
        this.setUserService(userService);
    }

    @Activate
    public void activate() {
        Services.formatter(this);
    }

    @Deactivate
    public void deactivate() {
        Services.formatter(null);
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService service) {
        this.threadPrincipalService = service;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String format(Instant instant) {
        Principal principal = this.threadPrincipalService.getPrincipal();
        return DateTimeFormatGenerator
                .getDateFormatForUser(
                        DateTimeFormatGenerator.Mode.SHORT,
                        DateTimeFormatGenerator.Mode.SHORT,
                        userService.getUserPreferencesService(),
                        principal)
                .format(instant);
    }

}