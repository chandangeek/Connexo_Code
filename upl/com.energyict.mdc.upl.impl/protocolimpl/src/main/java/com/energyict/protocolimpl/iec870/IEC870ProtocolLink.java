/*
 * ProtocolLink.java
 *
 * Created on 25 april 2003, 9:27
 */

package com.energyict.protocolimpl.iec870;

import java.io.*;
import java.util.*;
import com.energyict.cbo.*;
import java.math.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;

/**
 *
 * @author  Koen
 */
public interface IEC870ProtocolLink {
    public IEC870Connection getIEC870Connection();
    public TimeZone getTimeZone();
    public int getNumberOfChannels() throws UnsupportedException, IOException;
    public String getPassword();
    public int getProfileInterval() throws UnsupportedException, IOException;
}
