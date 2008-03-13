/*
 * ProtocolLink.java
 *
 * Created on 24 maart 2004, 17:40
 */

package com.energyict.protocolimpl.pact.core.common;

import java.util.*;
import java.util.logging.Logger;
import com.energyict.protocolimpl.pact.core.common.*;
import com.energyict.protocol.UnsupportedException;
/**
 *
 * @author  Koen
 */
public interface ProtocolLink {
    public TimeZone getTimeZone();
    public TimeZone getRegisterTimeZone();
    public PACTConnection getPactConnection();
    public ChannelMap getChannelMap();
    public int getProfileInterval() throws UnsupportedException, java.io.IOException;
    public PACTToolkit getPACTToolkit();
    public PACTMode getPACTMode();
    public Logger getLogger();
    public boolean isExtendedLogging();
    public boolean isStatusFlagChannel();
    public int getForcedRequestExtraDays();
    public int getModulo();
    public boolean isMeterTypeICM200();
    public boolean isMeterTypeCSP();
}
