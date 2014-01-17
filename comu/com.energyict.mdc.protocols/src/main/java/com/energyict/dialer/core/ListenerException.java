/*
 * ListenerException.java
 *
 * Created on 27 mei 2005, 15:26
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.dialer.core;

import com.energyict.mdc.protocol.api.dialer.core.LinkException;

/**
 * @author Koen
 */
public class ListenerException extends LinkException {

    public ListenerException(String str) {
        super(str);
    }

    public ListenerException(Throwable e) {
        super(e);
    }

    public ListenerException() {
        super();
    }

}