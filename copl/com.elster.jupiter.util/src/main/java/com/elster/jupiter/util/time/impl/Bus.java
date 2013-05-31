package com.elster.jupiter.util.time.impl;

import com.elster.jupiter.util.time.Clock;

/**
 * Copyrights EnergyICT
 * Date: 27/05/13
 * Time: 15:46
 */
enum Bus {
    ;

    public static final String COMPONENTNAME = "TIM";
    
    private static volatile ServiceLocator locator;

    static void setServiceLocator(ServiceLocator locator) {
        Bus.locator = locator;
    }

    public static Clock getClock() {
        return locator.getClock();
    }

}
