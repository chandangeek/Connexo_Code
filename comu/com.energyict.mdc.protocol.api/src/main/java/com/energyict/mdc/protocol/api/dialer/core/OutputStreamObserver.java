/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * InputStreamObserver.java
 *
 * Created on 6 oktober 2002, 12:45
 */

package com.energyict.mdc.protocol.api.dialer.core;

/**
 * @author Karel
 */
public interface OutputStreamObserver {

    void wrote(byte[] b);

    void threw(Throwable ex);

}
