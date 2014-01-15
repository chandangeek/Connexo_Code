/**
 * <p>Title: DialerException </p>
 * <p>Created on 13 april 2004, 9:44</p>
 * <p>Description: Baseclass for exceptions specific to the dialer mechanism </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 * <p>changes:</P>
 * <p>KV 13042004 initial version </p>
 *
 */

package com.energyict.mdc.protocol.api.dialer.core;


/**
 * @author Koen
 */
public class DialerException extends LinkException {

    /**
     * @param str
     */
    public DialerException(String str) {
        super(str);
    }

    /**
     * @param e
     */
    public DialerException(Throwable e) {
        super(e);
    }

    /**
     * {@link DialerException} default constructor
     */
    public DialerException() {
        super();
    }

}