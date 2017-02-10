/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.naming;

/**
 * Overview of the component names that are used
 * by the {@link com.elster.jupiter.cps.PersistenceSupport} classes
 * for each of the {@link com.elster.jupiter.cps.CustomPropertySet}s
 * that are contained in this protocol bundles.
 * <p>
 * This overview is important because each CustomPropertySet needs
 * to have a unique {@link com.elster.jupiter.orm.DataModel}
 * and the component name determines the name of that DataModel.
 * <p>
 * If you are creating a completely new CustomPropertySet then add a new
 * enum element and refer to it from the related PersistenceSupport class.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-02 (16:05)
 */
public enum CustomPropertySetComponentName {
    P01, P02, P03, P04, P05, P06, P07, P08, P09, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, P21, P22, P23,
    P24, P25, P26, P27, P28, P29, P30, P31, P32, P33, P34, P35, P36;
}