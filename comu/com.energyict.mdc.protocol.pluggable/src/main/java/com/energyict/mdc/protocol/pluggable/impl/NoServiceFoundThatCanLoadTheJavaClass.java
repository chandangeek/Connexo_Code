/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

/**
* Models the exceptional situation that occurs when
* no service component could be found that is capable
* of loading a particular java class.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2014-10-09 (13:36)
*/
class NoServiceFoundThatCanLoadTheJavaClass extends RuntimeException {

    NoServiceFoundThatCanLoadTheJavaClass(String javaClassname) {
        super("No deviceprotocol service found that can load '" + javaClassname + "'");
    }

}