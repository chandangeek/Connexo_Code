package com.elster.jupiter.util.time.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static com.elster.jupiter.util.Checks.is;

/**
 * Osgi component that implements the Clock interface.
 */
@Component(name = "com.elster.jupiter.time.clock",
        service = ClockProvider.class,
        property = {"osgi.command.scope=time",
                "osgi.command.function=setDateTime",
                "osgi.command.function=dateTime",
        },
        immediate = true)
public class ClockProvider  {

    private static final String BACK_TO_THE_FUTURE_MODE = "clock.back.to.the.future";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    private volatile ServiceRegistration<Clock> registration;
    private volatile Clock clock;

    public ClockProvider() {
    }

    @Activate
    public void activate(BundleContext context) {
        try {
            String backToTheFuture = context.getProperty(BACK_TO_THE_FUTURE_MODE);
            if (!is(backToTheFuture).emptyOrOnlyWhiteSpace() && Boolean.parseBoolean(backToTheFuture)) {
                clock = new OffsetClock(Clock.systemDefaultZone());
                registration = context.registerService(Clock.class, clock, null);
            } else {
                clock = Clock.systemDefaultZone();
                registration = context.registerService(Clock.class, clock, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Deactivate
    public void deactivate() {
    	registration.unregister();
    }

    public void setDateTime(String dateTime) {
        try {
            if (clock instanceof OffsetClock) {
                OffsetClock offsetClock = (OffsetClock) clock;
                offsetClock.set(LocalDateTime.from(DATE_TIME_FORMATTER.parse(dateTime)));
            } else {
                System.out.println("System's clock cannot be manipulated in current mode.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("usage : setDateTime \"yyyy/MM/dd HH:mm:ss\"");
        }
    }

    public void setDateTime(String date, String time) {
        try {
            if (clock instanceof OffsetClock) {
                OffsetClock offsetClock = (OffsetClock) clock;
                LocalDate localDate = LocalDate.from(DATE_FORMATTER.parse(date));
                LocalTime localTime = LocalTime.from(TIME_FORMATTER.parse(time));
                offsetClock.set(LocalDateTime.of(localDate, localTime));
            } else {
                System.out.println("System's clock cannot be manipulated in current mode.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("usage : setDateTime yyyy/MM/dd HH:mm:ss");
        }
    }

    public void dateTime() {
        System.out.println(DATE_TIME_FORMATTER.format(ZonedDateTime.ofInstant(clock.instant(), clock.getZone())));
    }
}
