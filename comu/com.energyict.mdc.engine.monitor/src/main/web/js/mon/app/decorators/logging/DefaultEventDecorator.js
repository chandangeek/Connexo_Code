/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.decorators.logging.DefaultEventDecorator', {
    alternateClassName: ['DefaultEventDecorator'],
    config: {
        event: undefined,
        occurrenceDateAsString: undefined
    },
    constructor: function(event) {
        this.setEvent(event);
        // the event's timestamp field contains the date as a String formatted by the backend:
        this.setOccurrenceDateAsString(this.getEvent().timestamp);
        return this;
    },
    asLogString: function() {
        var logString = this.occurrenceDateAsHTMLString() + ' - ' + this.getEvent()['class'] + ' {',
            attrib;
        for (attrib in this.getEvent()) {
            if (attrib !== 'timestamp' && attrib !== 'class') {
                logString += '\'' + attrib + '\': ' + this.getEvent()[attrib] + ', ';
            }
        }
        logString = logString.substring(0, logString.length - 2); // Subtract the last ', '
        logString += '}';
        return logString;
    },
    occurrenceDateAsHTMLString: function() {
        var htmlString = '<span class="logrecord-date">';
        htmlString += this.getOccurrenceDateAsString();
        htmlString += '</span>';
        return htmlString;
    }

});