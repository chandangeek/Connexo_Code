package com.energyict.mdc.device.lifecycle.config.impl;

import com.elster.jupiter.time.TimeDuration;

/**
 * Defines default and maximum values for maximum effective time shift.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-23 (16:53)
 */
public enum EffectiveTimeShift {

    FUTURE {
        @Override
        public TimeDuration maximumValue() {
            return TimeDuration.days(1);
        }

        @Override
        public TimeDuration defaultValue() {
            return TimeDuration.days(0);
        }
    },

    PAST {
        @Override
        public TimeDuration maximumValue() {
            return TimeDuration.days(30);
        }

        @Override
        public TimeDuration defaultValue() {
            return TimeDuration.days(0);
        }
    };

    public abstract TimeDuration maximumValue();

    public abstract TimeDuration defaultValue();

}