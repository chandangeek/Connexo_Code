package com.energyict.mdc.engine.impl.tools;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 10:49:39
 */
public class JmxUtils {

    private static final Logger logger = Logger.getLogger(JmxUtils.class.getPackage().getName());

    private JmxUtils() {
    }

    public static void registerMBean(Object object, ObjectName objectName) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        if (server != null) {
            try {
                server.registerMBean(object, objectName);
            } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                logger.severe("Caught exception while registering MBean " + e.getMessage());
                logger.log(Level.FINE, "Caught exception while registering MBean " + e.getMessage(), e);
            }
        }
    }

    public static void unRegisterMBean(ObjectName objectName) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        if (server != null) {
            try {
                server.unregisterMBean(objectName);
            } catch (MBeanRegistrationException | InstanceNotFoundException e) {
                logger.severe("Caught exception while unregistering MBean " + e.getMessage());
                logger.log(Level.FINE, "Caught exception while unregistering MBean " + e.getMessage(), e);
            }
        }
    }

    public static void invokeOperation(ObjectName objectName, String operationName) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        if (server != null) {
            try {
                if (server.isRegistered(objectName)) {
                    server.invoke(objectName, operationName, null, null);
                }
            } catch (MBeanRegistrationException | InstanceNotFoundException e) {
                logger.severe("Caught exception while unregistering MBean " + e.getMessage());
                logger.log(Level.FINE, "Caught exception while unregistering MBean " + e.getMessage(), e);
            } catch (ReflectionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (MBeanException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

}