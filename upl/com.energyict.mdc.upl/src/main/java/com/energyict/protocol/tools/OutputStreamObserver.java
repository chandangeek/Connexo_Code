/*
 * InputStreamObserver.java
 *
 * Created on 6 oktober 2002, 12:45
 */

package com.energyict.protocol.tools;

/**
 * @author Karel
 */
public interface OutputStreamObserver {

    void wrote(byte[] b);

    void threw(Throwable ex);

}