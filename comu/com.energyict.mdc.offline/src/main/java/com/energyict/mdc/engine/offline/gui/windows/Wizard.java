/*
 * Wizard.java
 *
 * Created on 30 maart 2005, 8:49
 */

package com.energyict.mdc.engine.offline.gui.windows;

/**
 * @author pasquien
 */
public interface Wizard {

    /** what to do to start to wizard */
    /**
     * first step
     */
    void start();

    /**
     * next step
     */
    void next();

    /**
     * previous step
     */
    void previous();

    /**
     * last step
     */
    void finish();

    /**
     * what to do when the wizard is canceled
     */
    void cancel();

    /**
     * Was the wizard canceled prematurely
     */
    boolean isCanceled();

}
