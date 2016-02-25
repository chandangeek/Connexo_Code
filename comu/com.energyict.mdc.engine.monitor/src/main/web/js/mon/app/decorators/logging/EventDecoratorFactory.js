Ext.define('CSMonitor.decorators.logging.EventDecoratorFactory', {
    alternateClassName: ['EventDecoratorFactory'],
    requires: ['CSMonitor.decorators.logging.DefaultEventDecorator', 'CSMonitor.decorators.logging.LoggingEventDecorator', 'CSMonitor.decorators.logging.ReadWriteEventDecorator'],
    singleton: true,
    decorate: function (event) {
        if (event['class']) {
            if (event['class'].indexOf("LoggingEvent") >= 0 || event['class'].indexOf("MeterDataStorageEvent") >= 0) {
                return new LoggingEventDecorator(event);
            }
            if (event['class'] === 'ReadEvent' || event['class'] === 'WriteEvent') {
                return new ReadWriteEventDecorator(event);
            }
            return new DefaultEventDecorator(event);
        }
    }
});




