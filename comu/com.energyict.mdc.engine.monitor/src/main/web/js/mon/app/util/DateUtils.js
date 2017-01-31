/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

// For the moment, this class is no longer used
// I keep it as a place holder for future Date util functions
Ext.define('CSMonitor.util.DateUtils', {
    alternateClassName: ['DateUtils'],
    singleton: true,
    config: {dateTimeSeparator: ' at '},
    isValid: function(theDate) {
        return !isNaN(theDate.valueOf());
    },
    renderer: function(theDate) {
        if (!this.isValid(theDate)) { // invalid date
            return '<time datetime="' + theDate + '"/>';
        }
        return this.toLocaleDateTime(theDate);
    },
    toLocaleDateTime: function(theDate) {
        return '<time datetime="' + theDate + '">' + theDate.toLocaleDateString() + this.dateTimeSeparator + theDate.toLocaleTimeString() + '</time>';
    }
});