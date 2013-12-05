/*
 * DataParseException.java
 *
 * Created on 29 oktober 2004, 9:25
 */

package com.energyict.protocolimpl.base;

import java.io.IOException;
/**
 *
 * @author  Koen
 */
public class DataParseException extends IOException {

    /** Creates a new instance of DataParseException */
    public DataParseException() {
        super();
    }

    public DataParseException(String s) {
        super(s);
    }
}
