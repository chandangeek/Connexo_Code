package com.energyict.mdc.engine.impl.core.online;

import com.energyict.mdc.engine.impl.core.aspects.performance.LoggingStopWatch;
import com.energyict.mdc.engine.impl.logging.PerformanceLogger;

/**
 * Defines pointcuts and advice to monitor the performance of the {@link ComServerDAOImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-10-09 (08:57)
 */
public aspect ComServerDAOPerformance {

    private pointcut useSqlConnection (ComServerDAOImpl comServerDAO):
           target(comServerDAO)
        && (   execution(private java.lang.Object execute (com.energyict.mdc.common.Transaction))
            || execution(public com.energyict.mdc.engine.config.ComServer getThisComServer())
            || execution(public com.energyict.mdc.engine.config.ComServer getComServer(java.lang.String))
            || execution(public com.energyict.mdc.engine.config.ComServer refreshComServer(java.lang.String))
            || execution(public com.energyict.mdc.engine.config.ComPort refreshComPort(java.lang.String))
            || execution(public java.util.List findExecutableOutboundComTasks(com.energyict.mdc.engine.config.OutboundComPort))
            || execution(public java.util.List findExecutableInboundComTasks(com.energyict.mdc.protocol.api.device.offline.OfflineDevice, com.energyict.mdc.engine.config.InboundComPort))
            || execution(public com.energyict.mdc.protocol.api.device.offline.OfflineDevice findDevice(com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier))
            || execution(public com.energyict.mdc.protocol.api.device.offline.OfflineRegister findRegister(com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier))
            || execution(public com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage findDeviceMessage(com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier))
            || execution(public boolean attemptLock(com.energyict.mdc.device.data.tasks.OutboundConnectionTask, com.energyict.mdc.engine.config.ComServer))
            || execution(public boolean attemptLock(com.energyict.mdc.device.data.tasks.ScheduledConnectionTask, com.energyict.mdc.engine.config.ComServer))
            || execution(public boolean attemptLock(com.energyict.mdc.device.data.tasks.ComTaskExecution, com.energyict.mdc.engine.config.ComPort))
            || execution(public boolean isStillPending(int))
            || execution(public boolean areStillPending(java.util.Collection))
            || execution(public java.util.List getDeviceProtocolSecurityProperties(com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier, com.energyict.mdc.engine.config.InboundComPort))
            || execution(public com.energyict.mdc.common.TypedProperties getDeviceConnectionTypeProperties(com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier, com.energyict.mdc.engine.config.InboundComPort))
            || execution(public com.energyict.mdc.common.TypedProperties getDeviceProtocolProperties(com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier)));

    Object around (ComServerDAOImpl comServerDAO): useSqlConnection(comServerDAO) {
        LoggingStopWatch stopWatch = new LoggingStopWatch("ComServerDAOImpl.useSqlConnection", PerformanceLogger.INSTANCE);
        Object result = proceed(comServerDAO);
        stopWatch.stop();
        return result;
    }

}