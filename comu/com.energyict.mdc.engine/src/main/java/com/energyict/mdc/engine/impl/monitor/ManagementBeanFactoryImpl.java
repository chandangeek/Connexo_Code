/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.time.Clock;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
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
    private volatile Thesaurus thesaurus;
    private final Map<ObjectName, ServiceRegistration> registeredMBeans = new HashMap<>();
    private volatile BundleContext context;

    @Activate
    public void activate(BundleContext context) {
        this.context = context;
    }

    @SuppressWarnings("unused")
    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @SuppressWarnings("unused")
    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(EngineService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Override
    public ComServerMonitorImplMBean findOrCreateFor(RunningComServer runningComServer) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(runningComServer);
            ServiceRegistration registeredMBean = this.registeredMBeans.get(jmxName);
            ComServerMonitorImplMBean comServerMBean;
            if (registeredMBean == null) {
                comServerMBean = new ComServerMonitorImpl(runningComServer, this.clock, this.thesaurus);
                this.registerMBean(comServerMBean, jmxName);
            } else {
                comServerMBean = (ComServerMonitorImplMBean) context.getService(registeredMBean.getReference());
            }
            return comServerMBean;
        }
    }

    @Override
    public void renamed(String oldName, RunningComServer runningComServer) {
        synchronized (this.registeredMBeans) {
            ObjectName oldJmxName = this.toComServerName(oldName);
            Object registeredMBean = this.registeredMBeans.get(oldJmxName);
            if (registeredMBean == null) {
                LOGGER.severe("Unable to find ComServerMonitorMBean for renamed online comserver " + oldName);
            } else {
                this.unRegisterMBean(oldJmxName);
                ObjectName newJmxName = this.nameFor(runningComServer);
                this.registerMBean(registeredMBean, newJmxName);
                LOGGER.finest("Online comserver \'" + oldJmxName + "\' renamed to " + newJmxName );
            }
        }
    }

    private void registerMBean(Object object, ObjectName objectName) {
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("jmx.objectname", objectName.toString());
        ServiceRegistration<?> serviceRegistration = context.registerService(object.getClass().getName(), object, properties);
        synchronized (this.registeredMBeans) {
            this.registeredMBeans.put(objectName, serviceRegistration);
        }

    }

    @Override
    public Optional<ComServerMonitorImplMBean> findFor(ComServer comServer) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(comServer);
            ServiceRegistration registeredMBean = this.registeredMBeans.get(jmxName);
            if (registeredMBean == null) {
                LOGGER.severe("Unable to find ComServerMonitorMBean for online comserver " + comServer.getName());
                return Optional.empty();
            } else {
                return Optional.of((ComServerMonitorImplMBean) context.getService(registeredMBean.getReference()));
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
        ServiceRegistration remove = this.registeredMBeans.remove(objectName);
        remove.unregister();
    }

    @Override
    public ScheduledComPortMonitorImplMBean findOrCreateFor(ScheduledComPort comPort) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(comPort.getComPort());
            ServiceRegistration registeredMBean = this.registeredMBeans.get(jmxName);
            ScheduledComPortMonitorImplMBean comPortMBean;
            if (registeredMBean == null) {
                comPortMBean = new ScheduledComPortMonitorImpl(comPort, this.clock, this.thesaurus);
                this.registerMBean(comPortMBean, jmxName);
            } else {
                comPortMBean = (ScheduledComPortMonitorImpl) context.getService(registeredMBean.getReference());
            }
            return comPortMBean;
        }
    }

    @Override
    public Optional<ScheduledComPortMonitorImplMBean> findFor(OutboundComPort comPort) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(comPort);
            ServiceRegistration registeredMBean = this.registeredMBeans.get(jmxName);
            if (registeredMBean == null) {
                LOGGER.severe("Unable to find ComPortMonitorMBean for outbound comport " + comPort.getName());
                return Optional.empty();
            } else {
                return Optional.of((ScheduledComPortMonitorImplMBean) context.getService(registeredMBean.getReference()));
            }
        }
    }

    @Override
    public void removeIfExistsFor(ScheduledComPort comPort) {
        removeIfExistsFor(this.nameFor(comPort.getComPort()));
    }

    @Override
    public InboundComPortMonitorImplMBean findOrCreateFor(ComPortListener inboundComPort) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(inboundComPort.getComPort());
            ServiceRegistration registeredMBean = this.registeredMBeans.get(jmxName);
            InboundComPortMonitorImplMBean comPortMBean;
            if (registeredMBean == null) {
                comPortMBean = new InboundComPortMonitorImpl(inboundComPort, this.clock, this.thesaurus);
                this.registerMBean(comPortMBean, jmxName);
            } else {
                comPortMBean = (InboundComPortMonitorImplMBean) context.getService(registeredMBean.getReference());
            }
            return comPortMBean;
        }
    }

    @Override
    public Optional<InboundComPortMonitorImplMBean> findFor(InboundComPort inboundComPort) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = this.nameFor(inboundComPort);
            ServiceRegistration registeredMBean = this.registeredMBeans.get(jmxName);
            if (registeredMBean == null) {
                LOGGER.severe("Unable to find ComPortMonitorMBean for inbound comport " + inboundComPort.getName());
                return Optional.empty();
            } else {
                return Optional.of((InboundComPortMonitorImplMBean) context.getService(registeredMBean.getReference()));
            }
        }
    }

    @Override
    public void removeIfExistsFor(ComPortListener inboundComPort) {
        removeIfExistsFor(this.nameFor(inboundComPort.getComPort()));
    }

    private void removeIfExistsFor(ObjectName jmxName) {
        synchronized (this.registeredMBeans) {
            Object registeredMBean = this.registeredMBeans.get(jmxName);
            if (registeredMBean != null) {
                this.unRegisterMBean(jmxName);
            }
        }
    }

    private ObjectName nameFor(RunningComServer comServer) {
        return nameFor(comServer.getComServer());
    }

    private ObjectName nameFor(ComServer comServer) {
        try {
            return new ObjectName(this.comServerBaseName(comServer));
        } catch (MalformedObjectNameException e) {
            throw CodingException.malformedObjectName(comServer, e, MessageSeeds.MBEAN_OBJECT_FORMAT);
        }
    }

    private ObjectName toComServerName(String comServerName) {
        try {
            return new ObjectName(this.comServerBaseName(comServerName));
        } catch (MalformedObjectNameException e) {
            throw CodingException.malformedComServerObjectName(comServerName, e, MessageSeeds.MBEAN_OBJECT_FORMAT);
        }
    }

    /**
     * Returns the base name of components that relate to the specified ComServer.
     *
     * @param comServer The ComServer
     * @return The base name
     */
    private String comServerBaseName(ComServer comServer) {
        return this.comServerBaseName(comServer.getName());
    }

    /**
     * Returns the base name of components that relate to the specified ComServer.
     *
     * @param comServerName The name of the ComServer
     * @return The base name
     */
    private String comServerBaseName(String comServerName) {
        return "Connexo-MultiSense:type=Communication server,name=" + comServerName;
    }

    private ObjectName nameFor(OutboundComPort comPort) {
        try {
            return new ObjectName(this.comServerBaseName(comPort.getComServer()) + ",process=Outbound communication ports,comPortName=" + comPort.getName());
        } catch (MalformedObjectNameException e) {
            throw CodingException.malformedObjectName(comPort, e, MessageSeeds.MBEAN_OBJECT_FORMAT);
        }
    }

    private ObjectName nameFor(InboundComPort comPort) {
        try {
            return new ObjectName(this.comServerBaseName(comPort.getComServer()) + ",process=Inbound communication ports,comPortName=" + comPort.getName());
        } catch (MalformedObjectNameException e) {
            throw CodingException.malformedObjectName(comPort, e, MessageSeeds.MBEAN_OBJECT_FORMAT);
        }
    }

}