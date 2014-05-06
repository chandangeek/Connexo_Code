package com.energyict.mdc.engine.impl.core.online;

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
            || execution(public com.energyict.mdc.engine.model.ComServer getThisComServer())
            || execution(public com.energyict.mdc.engine.model.ComServer getComServer(java.lang.String))
            || execution(public com.energyict.mdc.engine.model.ComServer refreshComServer(java.lang.String))
            || execution(public com.energyict.mdc.engine.model.ComPort refreshComPort(java.lang.String))
            || execution(public java.util.List findExecutableOutboundComTasks(com.energyict.mdc.engine.model.OutboundComPort))
            || execution(public java.util.List findExecutableInboundComTasks(com.energyict.mdc.protocol.api.device.offline.OfflineDevice, com.energyict.mdc.engine.model.InboundComPort))
            || execution(public com.energyict.mdc.protocol.api.device.offline.OfflineDevice findDevice(com.energyict.mdc.protocol.api.inbound.DeviceIdentifier))
            || execution(public com.energyict.mdc.protocol.api.device.offline.OfflineRegister findRegister(com.energyict.mdc.meterdata.identifiers.RegisterIdentifier))
            || execution(public com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage findDeviceMessage(com.energyict.mdc.meterdata.identifiers.MessageIdentifier))
            || execution(public boolean attemptLock(com.energyict.mdc.tasks.OutboundConnectionTask, com.energyict.mdc.engine.model.ComServer))
            || execution(public boolean attemptLock(com.energyict.mdc.tasks.ComTaskExecution, com.energyict.mdc.engine.model.ComPort))
            || execution(public boolean isStillPending(int))
            || execution(public boolean areStillPending(java.util.Collection))
            || execution(public java.util.List getDeviceProtocolSecurityProperties(com.energyict.mdc.protocol.api.inbound.DeviceIdentifier, com.energyict.mdc.engine.model.InboundComPort))
            || execution(public com.energyict.mdc.common.TypedProperties getDeviceConnectionTypeProperties(com.energyict.mdc.protocol.api.inbound.DeviceIdentifier, com.energyict.mdc.engine.model.InboundComPort))
            || execution(public com.energyict.mdc.common.TypedProperties getDeviceProtocolProperties(com.energyict.mdc.protocol.api.inbound.DeviceIdentifier)));

    Object around (ComServerDAOImpl comServerDAO): useSqlConnection(comServerDAO) {
        LoggingStopWatch stopWatch = new LoggingStopWatch("ComServerDAOImpl.useSqlConnection", PerformanceLogger.INSTANCE);
        Object result = proceed(comServerDAO);
        stopWatch.stop();
        return result;
    }

}