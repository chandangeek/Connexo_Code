package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.engine.impl.core.ComPortListener;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.core.ScheduledComPort;
import com.energyict.mdc.engine.impl.tools.JmxUtils;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.monitor.ComServerMonitorImplMBean;
import com.energyict.mdc.engine.monitor.InboundComPortMBean;
import com.energyict.mdc.engine.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.monitor.OutboundComPortMBean;

import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides an implementation for the {@link ManagementBeanFactory} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (13:32)
 */
public class ManagementBeanFactoryImpl implements ManagementBeanFactory {

    private static ManagementBeanFactory soleInstance;
    private Map<ObjectName, Object> registeredMBeans = new HashMap<>();

    public static synchronized ManagementBeanFactory getInstance () {
        if (soleInstance == null) {
            soleInstance = new ManagementBeanFactoryImpl();
        }
        return soleInstance;
    }

    public static synchronized void setInstance (ManagementBeanFactory managementBeanFactory) {
        soleInstance = managementBeanFactory;
    }

    @Override
    public ComServerMonitorImplMBean findOrCreateFor (RunningComServer runningComServer) {
        synchronized (this.registeredMBeans) {
            ComServerMonitorImplMBean comServerMBean;
            ObjectName jmxName = ManagementBeanNamingConventions.nameFor(runningComServer);
            Object registeredMBean = this.registeredMBeans.get(jmxName);
            if (registeredMBean == null) {
                comServerMBean = new ComServerMonitorImpl(runningComServer);
                this.registeredMBeans.put(jmxName, comServerMBean);
                JmxUtils.registerMBean(comServerMBean, jmxName);
            }
            else {
                comServerMBean = (ComServerMonitorImplMBean) registeredMBean;
            }
            return comServerMBean;
        }
    }

    @Override
    public ComServerMonitorImplMBean findOrCreateFor (OnlineComServer onlineComServer) {
        synchronized (this.registeredMBeans) {
            for (ObjectName objectName : this.registeredMBeans.keySet()) {
                Object registeredMBean = this.registeredMBeans.get(objectName);
                if (registeredMBean instanceof ComServerMonitorImpl) {
                    ComServerMonitorImpl comServerMBean = (ComServerMonitorImpl) registeredMBean;
                    if (comServerMBean.getComServer().getComServer().equals(onlineComServer)) {
                        return comServerMBean;
                    }
                }
            }
            return null;
        }
    }

    @Override
    public void removeIfExistsFor (RunningComServer runningComServer) {
        synchronized (this.registeredMBeans) {
            ObjectName jmxName = ManagementBeanNamingConventions.nameFor(runningComServer);
            Object registeredMBean = this.registeredMBeans.get(jmxName);
            if (registeredMBean != null) {
                JmxUtils.unRegisterMBean(jmxName);
            }
        }
    }

    @Override
    public OutboundComPortMBean findOrCreateFor (ScheduledComPort outboundComPort) {
        return null;
    }

    @Override
    public InboundComPortMBean findOrCreateFor (ComPortListener inboundComPort) {
        return null;
    }
}