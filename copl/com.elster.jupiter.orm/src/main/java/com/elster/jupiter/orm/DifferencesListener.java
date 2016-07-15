package com.elster.jupiter.orm;

import aQute.bnd.annotation.ConsumerType;

/**
 * Copyrights EnergyICT
 * Date: 4/07/2016
 * Time: 9:45
 */
@ConsumerType
@FunctionalInterface
public interface DifferencesListener {
    void onDifference(Difference difference);

    default void done() {
        // do nothing be default
    }
}
