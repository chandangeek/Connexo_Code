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

    fields: ['name', 'value'],

    listeners: {
       load: function(store, records) {
           Ext.Array.each(records, function(item) {
               if(item.get('value') === 'weeks') {
                   var translatedDayOfWeek = Uni.I18n.translate('general.day.monday', 'UNI', 'Monday');
                   switch(moment().weekday(0).format('dddd')) {
                       case 'Sunday':
                           translatedDayOfWeek = Uni.I18n.translate('general.day.sunday', 'UNI', 'Sunday');
                           break;
                          case 'Tuesday':
                           translatedDayOfWeek = Uni.I18n.translate('general.day.tuesday', 'UNI', 'Tuesday');
                           break;
                       case 'Wednesday':
                           translatedDayOfWeek = Uni.I18n.translate('general.day.wednesday', 'UNI', 'Wednesday');
                           break;
                       case 'Thursday':
                           translatedDayOfWeek = Uni.I18n.translate('general.day.thursday', 'UNI', 'Thursday');
                           break;
                       case 'Friday':
                           translatedDayOfWeek = Uni.I18n.translate('general.day.friday', 'UNI', 'Friday');
                           break;
                       case 'Saturday':
                           translatedDayOfWeek = Uni.I18n.translate('general.day.saturday', 'UNI', 'Saturday');
                           break;
                   }
                   item.data.name = Uni.I18n.translate('period.weeks.startsOn', 'UNI', 'week(s) (starts on ') + translatedDayOfWeek + ')';
               }
           });
       }
    }
});