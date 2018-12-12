/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Created by dragos on 1/29/2016.
 */
public class RemoteUtil {
    public static int findFreePort()
    {
        try
        {
            ServerSocket server = new ServerSocket(0);
            int port = server.getLocalPort();
            server.close();
            return port;
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
