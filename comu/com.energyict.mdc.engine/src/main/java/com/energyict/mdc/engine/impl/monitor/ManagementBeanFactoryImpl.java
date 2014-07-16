package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.monitor.InboundComPortMBean;
import com.energyict.mdc.engine.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.monitor.OutboundComPortMBean;

import com.elster.jupiter.util.time.Clock;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an implementation for the {@link ManagementBeanFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:32)
 */
@Component(name = "com.energyict.mdc.engine.mbeanfactory", service = ManagementBeanFactory.class)
public class ManagementBeanFactoryImpl implements ManagementBeanFactory {

    private static final Logger LOGGER = Logger.getLogger(ManagementBeanFactoryImpl.class.getName());

    private volatile Clock clock;
    private Map<ObjectName, Object> registeredMBeans = new HashMap<>();

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Override
    public ComServerMonitorImplMBean findOrCreateFor(RunningComServer runningComServer) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(runningComServer);
            Object registeredMBean = this.registeredMBeans.get(jmxName);
            ComServerMonitorImplMBean comServerMBean;
            if (registeredMBean == null) {
                comServerMBean = new ComServerMonitorImpl(runningComServer, this.clock);
                this.registerMBean(comServerMBean, jmxName);
            }
            else {
                comServerMBean = (ComServerMonitorImplMBean) registeredMBean;
            }
            return comServerMBean;
        }
    }

    private void registerMBean(Object object, ObjectName objectName) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        if (server != null) {
            this.registeredMBeans.put(objectName, object);
            try {
                server.registerMBean(object, objectName);
            }
            catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                LOGGER.severe("Caught exception while registering MBean " + e.getMessage());
                LOGGER.log(Level.FINE, "Caught exception while registering MBean " + e.getMessage(), e);
            }
        }
    }

    @Override
    public ComServerMonitorImplMBean findFor(OnlineComServer onlineComServer) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(onlineComServer);
            Object registeredMBean = this.registeredMBeans.get(jmxName);
            if (registeredMBean == null) {
                LOGGER.severe("Unable to find ComServerMonitorMBean for online comserver " + onlineComServer.getName());
                return null;
            }
            else {
                return (ComServerMonitorImplMBean) registeredMBean;
            }
        }
    }

    @Override
    public void removeIfExistsFor(RunningComServer runningComServer) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(runningComServer);
            Object registeredMBean = this.registeredMBeans.get(jmxName);
            if (registeredMBean != null) {
                this.unRegisterMBean(jmxName);
            }
        }
    }

    private void unRegisterMBean(ObjectName objectName) {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        if (server != null) {
            this.registeredMBeans.remove(objectName);
            try {
                server.unregisterMBean(objectName);
            }
            catch (MBeanRegistrationException | InstanceNotFoundException e) {
                LOGGER.severe("Caught exception while unregistering MBean " + e.getMessage());
                LOGGER.log(Level.FINE, "Caught exception while unregistering MBean " + e.getMessage(), e);
            }
        }
    }

    @Override
    public OutboundComPortMBean findOrCreateFor(ScheduledComPort outboundComPort) {
        return null;
    }

    @Override
    public InboundComPortMBean findOrCreateFor(ComPortListener inboundComPort) {
        return null;
    }

    private ObjectName nameFor(RunningComServer comServer) {
        return nameFor(comServer.getComServer());
    }

    private ObjectName nameFor(ComServer comServer) {
        try {
            return new ObjectName("EIServer:type=ComServer,name=" + comServer.getName());
        }
        catch (MalformedObjectNameException e) {
            throw CodingException.malformedObjectName(comServer, e);
        }
    }

}