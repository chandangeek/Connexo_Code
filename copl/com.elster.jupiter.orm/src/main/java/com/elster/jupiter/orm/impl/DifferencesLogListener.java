/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Difference;
import com.elster.jupiter.orm.DifferencesListener;

import java.util.logging.Level;
import java.util.logging.Logger;

class DifferencesLogListener implements DifferencesListener {
    private static final Logger LOGGER = Logger.getLogger(DifferencesLogListener.class.getName());

    private DifferencesListener state = difference -> {
        Listeners.FIRST.onDifference(difference);
        state = Listeners.SUBSEQUENT;
    };

    @Override
    public void onDifference(Difference difference) {
        state.onDifference(difference);
    }

    private enum Listeners implements DifferencesListener {
        FIRST {
            @Override
            public void onDifference(Difference difference) {
                LOGGER.log(Level.WARNING, "There are differences between DB and intended Data Model");
                super.onDifference(difference);
            }
        },
        SUBSEQUENT;

        @Override
        public void onDifference(Difference difference) {
            LOGGER.log(Level.INFO, difference::description);
        }
    }
}
