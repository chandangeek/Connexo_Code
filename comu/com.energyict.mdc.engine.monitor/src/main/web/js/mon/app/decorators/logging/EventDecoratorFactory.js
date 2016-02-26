Ext.define('CSMonitor.decorators.logging.EventDecoratorFactory', {
    alternateClassName: ['EventDecoratorFactory'],
    requires: ['CSMonitor.decorators.logging.DefaultEventDecorator', 'CSMonitor.decorators.logging.LoggingEventDecorator', 'CSMonitor.decorators.logging.ReadWriteEventDecorator'],
    singleton: true,
    LOGGING_EVENT_CLASSES: ['LoggingEvent', 'CollectedDeviceCacheEvent', 'CollectedDeviceTopologyEvent', 'CollectedFirmwareVersionEvent',
        'CollectedLoadProfileEvent', 'CollectedLogBookEvent', 'CollectedMessageListEvent', 'CollectedNoLogBooksForDeviceEvent', 'CollectedRegisterListEvent',
        'MeterDataStorageEvent', 'NoopCollectedDataEvent', 'StoreConfigurationEvent', 'UpdateDeviceIpAddressEvent', 'UpdateDeviceMessageEvent', 'UpdateDeviceProtocolPropertyEvent' ]
    ,
    decorate: function (event) {
        if (event['class']) {
            if (this.isLoggingEvent(event['class'])) {
                return new LoggingEventDecorator(event);
            }
            if (event['class'] === 'ReadEvent' || event['class'] === 'WriteEvent') {
                return new ReadWriteEventDecorator(event);
            }
            return new DefaultEventDecorator(event);
        }
    },
    isLoggingEvent: function (className){
        return (className.indexOf('LoggingEvent') >= 0 || this.LOGGING_EVENT_CLASSES.indexOf(className) >= 0);
    }
});




