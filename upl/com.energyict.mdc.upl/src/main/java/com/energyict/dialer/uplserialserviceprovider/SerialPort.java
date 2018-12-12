/*
 * Copyright (c) 2016 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.dialer.uplserialserviceprovider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface SerialPort {

    void init(SerialConfig serialConfig) throws IOException;

    InputStream getInputStream();

    OutputStream getOutputStream();

    void setWriteDrain(boolean writeDrain);

    void setRcvTimeout(int receiveTimeout) throws IOException;

    void configure(SerialConfig serialConfig) throws IOException;

    boolean sigCD() throws IOException;

    boolean sigCTS() throws IOException;

    boolean sigDSR() throws IOException;

    boolean sigRing() throws IOException;

    void setDTR(boolean dtr) throws IOException;

    void setRTS(boolean rts) throws IOException;

    int txBufCount() throws IOException;

    void close() throws IOException;

}
