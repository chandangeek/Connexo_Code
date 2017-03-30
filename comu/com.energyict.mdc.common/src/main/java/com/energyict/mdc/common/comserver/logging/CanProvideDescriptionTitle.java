/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver.logging;

/**
 * Models the behavior of component that can provide
 * a human readable title for a description of the component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-26 (11:26)
 */
public interface CanProvideDescriptionTitle {

    public String getDescriptionTitle ();

}