/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

class InvalidPasswordException extends Exception {

    private static final long serialVersionUID = 1L;

    InvalidPasswordException( String msg ){
        super( msg );
    }

}
