package com.energyict.mdc.common;

/**
 * Activates a {@link BusinessEventListener} with the {@link BusinessEventManager}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-21 (13:27)
 */
public interface BusinessEventListenerActivator {

    public void activateAt (BusinessEventManager businessEventManager);

}