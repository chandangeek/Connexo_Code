/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.decorators.logging.EventDecoratorFactory', {
    alternateClassName: ['EventDecoratorFactory'],
    requires: ['CSMonitor.decorators.logging.DefaultEventDecorator', 'CSMonitor.decorators.logging.LoggingEventDecorator', 'CSMonitor.decorators.logging.ReadWriteEventDecorator', 'CSMonitor.decorators.logging.CollectedDataEventDecorator',],
    singleton: true,
    COLLECTED_DATA_EVENT_CLASSES: ['CollectedDeviceCacheEvent', 'CollectedDeviceTopologyEvent', 'CollectedFirmwareVersionEvent',
        'CollectedLoadProfileEvent', 'CollectedLogBookEvent', 'CollectedMessageListEvent', 'CollectedNoLogBooksForDeviceEvent', 'CollectedRegisterListEvent',
        'MeterDataStorageEvent', 'NoopCollectedDataEvent', 'StoreConfigurationEvent', 'UpdateDeviceIpAddressEvent', 'UpdateDeviceMessageEvent', 'UpdateDeviceProtocolPropertyEvent']
    ,
    decorate: function (event) {
        if (event['class']) {
            if (event['class'].indexOf("LoggingEvent") >= 0) {
                return new LoggingEventDecorator(event);
            }
            if (event['class'] === 'ReadEvent' || event['class'] === 'WriteEvent') {
                return new ReadWriteEventDecorator(event);
            }
            if (this.isCollectedDataEvent(event['class'])) {
                return new CollectedDataEventDecorator(event);
            }
            return new DefaultEventDecorator(event);
        }
    },
    isCollectedDataEvent: function (className) {
        return this.COLLECTED_DATA_EVENT_CLASSES.indexOf(className) >= 0;
    }
});




