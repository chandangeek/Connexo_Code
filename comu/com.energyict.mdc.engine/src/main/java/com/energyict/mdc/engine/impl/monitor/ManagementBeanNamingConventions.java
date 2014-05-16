package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.core.RunningComServer;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Provides naming conventions utility methods for the {@link com.energyict.mdc.engine.monitor.ManagementBeanFactory}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:41)
 */
public final class ManagementBeanNamingConventions {

    public static ObjectName nameFor (RunningComServer comServer) {
        try {
            return new ObjectName("EIServer:type=ComServer,name=" + comServer.getComServer().getName());
        }
        catch (MalformedObjectNameException e) {
            throw CodingException.malformedObjectName(comServer, e);
        }
    }

    // Hide utility class constructor
    private ManagementBeanNamingConventions () {}

}