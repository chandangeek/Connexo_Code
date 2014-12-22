/**
 * @class Uni.DateTime
 */
Ext.define('Uni.DateTime', {
    singleton: true,

    requires: [
        'Ldr.store.Preferences'
    ],

    dateShortKey: 'format.date.long',
    dateLongKey: 'format.date.long',

    timeShortKey: 'format.time.long',
    timeLongKey: 'format.time.long',

    dateTimeShortKey: 'format.datetime.long',
    dateTimeLongKey: 'format.datetime.long',

    dateShortDefault: 'dd MMM \'yy',
    dateLongDefault: 'EEE dd MMM \'yy',

    timeShortDefault: 'HH:mm',
    timeLongDefault: 'HH:mm:ss',

    dateTimeShortDefault: this.dateShortDefault + ' ' + this.timeShortDefault,
    dateTimeLongDefault: this.dateLongDefault + ' ' + this.timeLongDefault,

    //<debug>
    // Used to only show missing preferences messages once.
    blacklist: [],
    //</debug>

    lookupPreference: function (key) {
        var me = this,
            preference = Ldr.store.Preferences.getById(key);

        if (typeof preference !== 'undefined' && preference !== null) {
            preference = preference.data.value;
        } else {
            //<debug>
            if (!me.blacklist[key]) {
                me.blacklist[key] = true;
                console.warn('Missing preference for key \'' + key + '\'.');
            }
            //</debug>
        }

        return preference;
    },

    format: function(key, fallback) {
        var preference = this.lookupPreference(key);

        if ((typeof preference === 'undefined' || preference === null)
            && typeof fallback === 'undefined' && fallback === null) {
            preference = key;
        }

        if ((typeof preference === 'undefined' || preference === null)
            && typeof fallback !== 'undefined' && fallback !== null) {
            preference = fallback;
        }

        return preference;
    },

    formatDateShort: function (date) {
        date = date || new Date();

        var me = this,
            format = me.format(me.dateShortKey, me.dateShortDefault);

        return Ext.Date.format(date, format);
    },

    formatDateLong: function (date) {
        date = date || new Date();

        var me = this,
            format = me.format(me.dateLongKey, me.dateLongDefault);

        return Ext.Date.format(date, format);
    },

    formatTimeShort: function (date) {
        date = date || new Date();

        var me = this,
            format = me.format(me.timeShortKey, me.timeShortDefault);

        return Ext.Date.format(date, format);
    },

    formatTimeLong: function (date) {
        var me = this,
            format = me.format(me.dateShortKey, me.dateShortDefault);

        date = date || new Date();

        return Ext.Date.format(date, format);
    },

    formatDateTimeShort: function (date) {
        var me = this,
            format = me.format(me.dateShortKey, me.dateShortDefault);

        date = date || new Date();

        return Ext.Date.format(date, format);
    },

    formatDateTimeLong: function (date) {
        var me = this,
            format = me.format(me.dateShortKey, me.dateShortDefault);

        date = date || new Date();

        return Ext.Date.format(date, format);
    }
});