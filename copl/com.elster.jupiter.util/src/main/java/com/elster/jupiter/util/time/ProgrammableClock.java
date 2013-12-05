package com.elster.jupiter.util.time;

import java.util.Date;
import java.util.TimeZone;

public class ProgrammableClock implements Clock {
    private enum DefaultSystem implements SystemAbstraction {
        INSTANCE;

        @Override
        public long currentTimeMillis() {
            return System.currentTimeMillis();
        }
    }

    private enum DefaultTimeZoneComponent implements TimeZoneComponent {
        INSTANCE;

        @Override
        public TimeZone getTimeZone() {
            return TimeZone.getDefault();
        }
    }

    interface SystemAbstraction {
        long currentTimeMillis();
    }

    private interface DateComponent {
        Date now();
    }

    private static class SpecifiedTimeZone implements TimeZoneComponent {
        private final TimeZone timeZone;

        @Override
        public TimeZone getTimeZone() {
            return timeZone;
        }

        private SpecifiedTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
        }
    }

    private static class DefaultDateComponent implements DateComponent {
        private final SystemAbstraction systemAbstraction;

        public DefaultDateComponent(SystemAbstraction systemAbstraction) {
            this.systemAbstraction = systemAbstraction;
        }

        @Override
        public Date now() {
            return new Date(systemAbstraction.currentTimeMillis());
        }
    }

    private interface TimeZoneComponent {
        TimeZone getTimeZone();
    }

    private static class FrozenDateComponent implements DateComponent {
        private final Date frozen;

        @Override
        public Date now() {
            return frozen;
        }

        FrozenDateComponent(Date frozen) {
            this.frozen = new Date(frozen.getTime());
        }
    }

    private static class OffsetDateComponent implements DateComponent {
        private final long offset;
        private final SystemAbstraction systemAbstraction;

        @Override
        public Date now() {
            return new Date(systemAbstraction.currentTimeMillis() + offset);
        }

        private OffsetDateComponent(long offset, SystemAbstraction systemAbstraction) {
            this.offset = offset;
            this.systemAbstraction = systemAbstraction;
        }
    }

    private final DateComponent dateComponent;
    private final TimeZoneComponent timeZoneComponent;
    private final SystemAbstraction systemAbstraction;

    public ProgrammableClock() {
        this(new DefaultDateComponent(DefaultSystem.INSTANCE), DefaultTimeZoneComponent.INSTANCE, DefaultSystem.INSTANCE);
    }

    ProgrammableClock(SystemAbstraction systemAbstraction) {
        this(new DefaultDateComponent(systemAbstraction), DefaultTimeZoneComponent.INSTANCE, systemAbstraction);
    }

    private ProgrammableClock(DateComponent dateComponent, TimeZoneComponent timeZoneComponent, SystemAbstraction systemAbstraction) {
        this.dateComponent = dateComponent;
        this.timeZoneComponent = timeZoneComponent;
        this.systemAbstraction = systemAbstraction;
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZoneComponent.getTimeZone();
    }

    @Override
    public Date now() {
        return dateComponent.now();
    }

    public ProgrammableClock frozenAt(Date date) {
        return new ProgrammableClock(new FrozenDateComponent(date), timeZoneComponent, systemAbstraction);
    }

    public ProgrammableClock inTimeZone(TimeZone timeZone) {
        return new ProgrammableClock(dateComponent, new SpecifiedTimeZone(timeZone), systemAbstraction);
    }

    public ProgrammableClock setNow(Date now) {
        long offset = now.getTime() - systemAbstraction.currentTimeMillis();
        return new ProgrammableClock(new OffsetDateComponent(offset, systemAbstraction), timeZoneComponent, systemAbstraction);
    }

    public ProgrammableClock toDefault() {
        return new ProgrammableClock();
    }

    public ProgrammableClock withOffset(long offset) {
        return new ProgrammableClock(new OffsetDateComponent(offset, systemAbstraction), timeZoneComponent, systemAbstraction);
    }

}
