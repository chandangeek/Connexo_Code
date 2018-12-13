/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl;

public class NotInObjectListException extends ProtocolException {

    public NotInObjectListException() {
        super();
    }

    public NotInObjectListException(String msg) {
        super(msg);
    }

    public NotInObjectListException(Exception e) {
        super(e);
    }

    public NotInObjectListException(Exception e, String msg) {
        super(e, msg);
    }

}