/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.dialer.serialserviceprovider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SerialPort {


    public void init(SerialConfig serialConfig) throws IOException;

    public InputStream getInputStream();

    public OutputStream getOutputStream();

    public void setWriteDrain(boolean writeDrain);

    public void setRcvTimeout(int receiveTimeout) throws IOException;

    public void configure(SerialConfig serialConfig) throws IOException;

    public boolean sigCD() throws IOException;

    public boolean sigCTS() throws IOException;

    public boolean sigDSR() throws IOException;

    public boolean sigRing() throws IOException;

    public void setDTR(boolean dtr) throws IOException;

    public void setRTS(boolean rts) throws IOException;

    public int txBufCount() throws IOException;

    public void close() throws IOException;

}
