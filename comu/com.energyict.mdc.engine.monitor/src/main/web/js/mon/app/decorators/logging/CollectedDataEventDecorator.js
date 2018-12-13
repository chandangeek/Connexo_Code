/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.decorators.logging.CollectedDataEventDecorator', {
    extend: 'CSMonitor.decorators.logging.DefaultEventDecorator',
    alternateClassName: ['CollectedDataEventDecorator'],
    constructor: function (event) {
        this.callParent(arguments);
    },
    asLogString: function () {
        var logString = this.occurrenceDateAsHTMLString() + ' - ' + this.getEvent()['log-level'] + ': ' + this.getEvent()['message'];
        if (this.getEvent()['details']) {
            logString += ': ' + JSON.stringify(this.getEvent()['details']);
        }
        if (this.getEvent()['issues']) {
            logString += ', issues: ';
            logString += JSON.stringify(this.getEvent()['issues']);
        }
        return logString;
    }
});