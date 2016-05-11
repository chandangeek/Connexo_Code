package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.EffectiveCalendar;

import com.google.common.collect.ImmutableMap;
import javax.inject.Inject;

import java.util.Map;

public abstract class EffectiveCalendarImpl implements EffectiveCalendar {

    public enum Fields {
        ID("id"),
        CALENDAR("allowedCalendar"),
        ACTIVATIONDATE("activationDate"),
        DEVICE("device"),
        INTERVAL("interval");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    // ORM inheritance map
    public static final Map<String, Class<? extends EffectiveCalendar>> IMPLEMENTERS = ImmutableMap.of(
            ActiveEffectiveCalendarImpl.TYPE_IDENTIFIER, ActiveEffectiveCalendarImpl.class,
            PassiveEffectiveCalendarImpl.TYPE_IDENTIFIER, PassiveEffectiveCalendarImpl.class);


    private long id;

    private Reference<AllowedCalendar> allowedCalendar = ValueReference.absent();
    private Reference<Device> device = ValueReference.absent();
    private Interval interval;

    @Override
    public AllowedCalendar getAllowedCalendar() {
        return this.allowedCalendar.orNull();
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }

    static Map<String, Class<? extends EffectiveCalendar>> getImplementers () {
        ImmutableMap.Builder<String, Class<? extends EffectiveCalendar>> builder = ImmutableMap.builder();
        builder.put(ActiveEffectiveCalendarImpl.TYPE_IDENTIFIER, ActiveEffectiveCalendarImpl.class)
                .put(PassiveEffectiveCalendarImpl.TYPE_IDENTIFIER, PassiveEffectiveCalendarImpl.class);

        return builder.build();
    }


    public EffectiveCalendarImpl init (AllowedCalendar allowedCalendar, Interval interval, Device device) {
        this.allowedCalendar.set(allowedCalendar);
        this.interval = interval;
        this.device.set(device);
        return this;
    }

    @Inject
    public EffectiveCalendarImpl() {

    }
}
