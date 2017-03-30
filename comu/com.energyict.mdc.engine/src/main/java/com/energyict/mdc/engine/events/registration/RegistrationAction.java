/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.events.registration;

/**
 * Models the behavior of components that will allow
 * client applications to register interests
 * for {@link com.energyict.mdc.engine.events.ComServerEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (13:02)
 */
public interface RegistrationAction {

    public void execute ();

}