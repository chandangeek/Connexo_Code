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

    dateTimeShortKey: 'format.datetime.short',
    dateTimeLongKey: 'format.datetime.long',

    dateShortDefault: 'd M \'y',
    dateLongDefault: 'D d M \'y',

    timeShortDefault: 'H:i',
    timeLongDefault: 'H:i:s',

    dateTimeShortDefault: 'd M \'y - H:i',
    dateTimeLongDefault: 'D d M \'y - H:i:s',

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
        date = date || new Date();

        var me = this,
            format = Uni.util.Preferences.lookup(me.dateTimeShortKey, me.dateTimeShortDefault);

        return Ext.Date.format(date, format);
    },

    formatDateTimeLong: function (date) {
        date = date || new Date();

        var me = this,
            format = Uni.util.Preferences.lookup(me.dateTimeLongKey, me.dateTimeLongDefault);

        return Ext.Date.format(date, format);
    }
});