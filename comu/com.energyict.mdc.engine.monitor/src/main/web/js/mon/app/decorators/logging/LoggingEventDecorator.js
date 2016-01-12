Ext.define('CSMonitor.decorators.logging.LoggingEventDecorator', {
    extend: 'CSMonitor.decorators.logging.DefaultEventDecorator',
    alternateClassName: ['LoggingEventDecorator'],
    constructor: function(event) {
        this.callParent(arguments);
    },
    asLogString: function() {
        return this.occurrenceDateAsHTMLString() + ' - ' + this.getEvent()['log-level'] + ': ' + this.getEvent()['message'];
    }
});