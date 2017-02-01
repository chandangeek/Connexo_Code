/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.DateTime
 */
Ext.define('Uni.DateTime', {
    singleton: true,

    requires: [
        'Uni.util.Preferences'
    ],

    dateShortKey: 'format.date.short',
    dateLongKey: 'format.date.long',

    timeShortKey: 'format.time.short',
    timeLongKey: 'format.time.long',

    dateTimeSeparatorKey: 'format.datetime.separator',
    dateTimeOrderKey: 'format.datetime.order',

    dateShortDefault: 'd M \'y',
    dateLongDefault: 'D d M \'y',

    timeShortDefault: 'H:i',
    timeLongDefault: 'H:i:s',

    dateTimeSeparatorDefault: '-',
    dateTimeOrderDefault: 'DT',

    LONG: 'long',
    SHORT: 'short',

    formatDateShort: function (date) {
        date = date || new Date();

        var me = this,
            format = Uni.util.Preferences.lookup(me.dateShortKey, me.dateShortDefault);

        return Ext.Date.format(date, format);
    },

    formatDateLong: function (date) {
        date = date || new Date();

        var me = this,
            format = Uni.util.Preferences.lookup(me.dateLongKey, me.dateLongDefault);

        return Ext.Date.format(date, format);
    },

    formatTimeShort: function (date) {
        date = date || new Date();

        var me = this,
            format = Uni.util.Preferences.lookup(me.timeShortKey, me.timeShortDefault);

        return Ext.Date.format(date, format);
    },

    formatTimeLong: function (date) {
        date = date || new Date();

        var me = this,
            format = Uni.util.Preferences.lookup(me.timeLongKey, me.timeLongDefault);

        return Ext.Date.format(date, format);
    },

    formatDateTimeShort: function (date) {
        return this.formatDateTime(date, this.SHORT, this.SHORT);
    },

    formatDateTimeLong: function (date) {
        return this.formatDateTime(date, this.LONG, this.LONG);
    },

    formatDateTime: function (date, dateLongOrShort, timeLongOrShort) {
        return this.doFormat(
            date,
            this.LONG === dateLongOrShort
                ? Uni.util.Preferences.lookup(this.dateLongKey, this.dateLongDefault)
                : Uni.util.Preferences.lookup(this.dateShortKey, this.dateShortDefault),
            this.LONG === timeLongOrShort
                ? Uni.util.Preferences.lookup(this.timeLongKey, this.timeLongDefault)
                : Uni.util.Preferences.lookup(this.timeShortKey, this.timeShortDefault)
        );
    },

    doFormat: function (date, dateFormat, timeFormat) {
        var me = this,
            dateTimeFormat,
            orderFormat = Uni.util.Preferences.lookup(me.dateTimeOrderKey, me.dateTimeOrderDefault),
            separatorFormat = Uni.util.Preferences.lookup(me.dateTimeSeparatorKey, me.dateTimeSeparatorDefault);

        date = date || new Date();
        if (Ext.String.startsWith(orderFormat, 'T'))  {
            dateTimeFormat = timeFormat;
        } else {
            dateTimeFormat = dateFormat;
        }
        dateTimeFormat += (' ' + separatorFormat.trim() + ' ');
        if (Ext.String.startsWith(orderFormat, 'T')) {
            dateTimeFormat += dateFormat;
        } else {
            dateTimeFormat += timeFormat;
        }
        return Ext.Date.format(Ext.isDate(date) ? date : new Date(date), dateTimeFormat);
    }
});