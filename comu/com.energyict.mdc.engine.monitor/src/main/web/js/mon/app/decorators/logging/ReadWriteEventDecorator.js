/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('CSMonitor.decorators.logging.ReadWriteEventDecorator', {
    extend: 'CSMonitor.decorators.logging.DefaultEventDecorator',
    alternateClassName: ['ReadWriteEventDecorator'],

    constructor: function(event) {
        this.callParent(arguments);
    },

    asLogString: function() {
        var logString = this.occurrenceDateAsHTMLString() + ' - ' + this.getEvent()['class'] + ': ',
            hex = this.getHex(),
            chars = this.getChar();

        if (this.getEvent()['bytes-read']) {
            logString += ' bytes read:' + this.getEvent()['bytes-read'];
        }
        if (this.getEvent()['bytes-written']) {
            logString += ' bytes written:' + this.getEvent()['bytes-written'];
        }
        if (hex.length > 0) {
            logString += '<br>HEX:<br>' + this.getHex();
        }
        if (chars.length > 0) {
            logString += '<br>DECIMAL:<br>' + chars;
        }
        return logString;
    },

    getDateAndTypePart: function() {
        return this.occurrenceDateAsHTMLString() + ' - ' + this.getType(this.getEvent()['class']);
    },

    getType: function(eventClass) {
        if ('ReadEvent' === eventClass) {
            return 'RX';
        }
        if ('WriteEvent' === eventClass) {
            return 'TX';
        }
        return '';
    },

    getHex: function() {
        var bytes = [], hex = [], i = 0;
        if (this.getEvent()['bytes-read']) {
            bytes = this.getEvent()['bytes-read'];
        }
        if (this.getEvent()['bytes-written']) {
            bytes = this.getEvent()['bytes-written'];
        }
        for (i = 0; i < bytes.length; i++) {
            hex[i] = (bytes[i] & 0xff).toString(16);
            if (hex[i].length === 1) {
                hex[i] = '0' + hex[i];
            }
        }
        return hex;
    },
    getChar: function() {
        var bytes = [], decimal = [], i, byteValue;
        if (this.getEvent()['bytes-read']) {
            bytes = this.getEvent()['bytes-read'];
        }
        if (this.getEvent()['bytes-written']) {
            bytes = this.getEvent()['bytes-written'];
        }
        for (i = 0; i < bytes.length; i++) {
            byteValue = bytes[i] & 0xff;
            if (byteValue < 33 || (byteValue > 127 && byteValue < 161)) {
                decimal[i] = '.';
            } else {
                decimal[i] = String.fromCharCode(bytes[i] & 0xff);
            }
        }
        return decimal;
    }
});