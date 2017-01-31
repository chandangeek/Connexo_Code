/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.decorators.logging.LoggingEventDecorator', {
    extend: 'CSMonitor.decorators.logging.DefaultEventDecorator',
    alternateClassName: ['LoggingEventDecorator'],
    constructor: function(event) {
        this.callParent(arguments);
    },
    asLogString: function() {
        if (!this.getEvent()['details']) {
            return this.occurrenceDateAsHTMLString() + ' - ' + this.getEvent()['log-level'] + ': ' + this.getEvent()['message'];
        }else{
            return this.occurrenceDateAsHTMLString() + ' - ' + this.getEvent()['log-level'] + ': ' + this.getEvent()['message'] + ': ' + Ext.decode(this.getEvent()['details'], true);
        }
    }
});