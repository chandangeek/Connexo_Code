/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.powermeasurement.ion;

import java.io.IOException;

/** This message is thrown when 
 * 
 * @author fbo
 */

class AuthenticationException extends IOException {

    private static final long serialVersionUID = 1L;

    AuthenticationException( String msg ){
        super( msg );
    }
    
}
