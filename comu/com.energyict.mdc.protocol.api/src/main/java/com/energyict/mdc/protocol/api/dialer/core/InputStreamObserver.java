/*
 * InputStreamObserver.java
 *
 * Created on 6 oktober 2002, 12:45
 */

package com.energyict.mdc.protocol.api.dialer.core;

/**
 * @author Karel
 */
public interface InputStreamObserver {

    void read(byte[] b);

    void threw(Throwable ex);

}
