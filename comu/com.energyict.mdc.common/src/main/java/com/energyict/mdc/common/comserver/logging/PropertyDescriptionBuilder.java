/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.comserver.logging;

/**
 * Assists in building the description of multi-value properties
 * with the {@link DescriptionBuilder}.
 * Will copy the interface of a StringBuilder
 * leaving out the methods that are not frequently used.
 * Talk to the developer of this class if you really need one of these.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-25 (09:36)
 */
public interface PropertyDescriptionBuilder {

    /**
     * Notifies this builder that the value under construction is complete
     * and that the building process of the next value will start.
     */
    public PropertyDescriptionBuilder next ();

    public PropertyDescriptionBuilder append (CharSequence s);
    public PropertyDescriptionBuilder append (String s);
    public PropertyDescriptionBuilder append (Object o);
    public PropertyDescriptionBuilder append (boolean b);
    public PropertyDescriptionBuilder append (char c);
    public PropertyDescriptionBuilder append (int i);
    public PropertyDescriptionBuilder append (long l);

}