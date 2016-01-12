/**
 * @class Uni.store.Periods
 */
Ext.define('Uni.store.Periods', {
    extend: 'Ext.data.ArrayStore',

    data: [
        [Uni.I18n.translate('period.months', 'UNI', 'month(s)'), 'months'],
        [Uni.I18n.translate('period.weeks', 'UNI', 'week(s)'), 'weeks'],
        [Uni.I18n.translate('period.days', 'UNI', 'day(s)'), 'days'],
        [Uni.I18n.translate('period.hours', 'UNI', 'hour(s)'), 'hours'],
        [Uni.I18n.translate('period.minutes', 'UNI', 'minute(s)'), 'minutes']
    ],

    fields: ['name', 'value']

});