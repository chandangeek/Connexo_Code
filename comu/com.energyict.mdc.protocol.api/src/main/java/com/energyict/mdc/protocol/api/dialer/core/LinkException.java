package com.energyict.mdc.protocol.api.dialer.core;

/**
 * <p>Title: LinkException </p>
 * <p>Created on 13 april 2004, 9:44</p>
 * <p>Description: Baseclass for exceptions specific to the dialer mechanism </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author unascribed
 * @version 1.0
 *          <p>changes:</P>
 *          <p>KV 13042004 initial version </p>
 */
public class LinkException extends Exception {

    /**
     * @param str
     */
    public LinkException(String str) {
        super(str);
    }

    /**
     * @param e
     */
    public LinkException(Throwable e) {
        super(e);
    }

    /**
     *
     */
    public LinkException() {
        super();
    }

}