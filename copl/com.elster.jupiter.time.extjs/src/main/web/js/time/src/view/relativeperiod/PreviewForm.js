Ext.define('Tme.view.relativeperiod.PreviewForm', {
    extend: 'Ext.form.Panel',
    alias: 'widget.relative-periods-preview-form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        me.items = [
            {
                fieldLabel: Uni.I18n.translate('relativeperiod.name', 'TME', 'Name'),
                name: 'name'
            },
            {
                fieldLabel: Uni.I18n.translate('relativeperiod.category', 'TME', 'Category'),
                name: 'listOfCategories'
            },
            {
                fieldLabel: Uni.I18n.translate('general.start', 'TME', 'Start'),
                name: 'from',
                renderer: function (value) {
                    return me.constructString(value);
                }
            },
            {
                fieldLabel: Uni.I18n.translate('general.end', 'TME', 'End'),
                name: 'to',
                renderer: function (value) {
                    return me.constructString(value)
                }
            }
        ];
        me.callParent(arguments);
    },

    constructString: function (value) {
        var str = '',
            hoursMinutes = (value.atHour ? value.atHour : '00') + ' ' + Uni.I18n.translate('period.hours', 'UNI', 'hour(s)') + ' ' + (value.atMinute ? value.atMinute : '00') + ' ' + Uni.I18n.translate('period.minutes', 'UNI', 'minute(s)');

        if (value.startNow) {
            str += Uni.I18n.translate('general.Now', 'TME', 'Now') + ' ' + Uni.I18n.translate('general.at', 'TME', 'at') + ' ' + hoursMinutes;
            return str;
        }
        if (value.startFixedDay && value.startFixedMonth && value.startFixedYear) {
            var fixedDate = moment([parseInt(value.startFixedYear), parseInt(value.startFixedMonth) - 1, parseInt(value.startFixedDay)]).format('ddd, DD MMM YYYY');
            str += Uni.I18n.translate('general.fixedDate', 'TME', 'fixed date') + ' ' + fixedDate + ' ' + Uni.I18n.translate('general.at', 'TME', 'at') + ' ' + hoursMinutes;
        } else if (value.startAmountAgo && value.startPeriodAgo && value.startTimeMode) {
            var startTimeMode,
                startPeriodAgo;

            switch (value.startPeriodAgo) {
                case 'months':
                    startPeriodAgo = Uni.I18n.translate('period.months', 'UNI', 'month(s)');
                    break;
                case 'weeks':
                    startPeriodAgo = Uni.I18n.translate('period.weeks', 'UNI', 'week(s)');
                    break;
                case 'days':
                    startPeriodAgo = Uni.I18n.translate('period.days', 'UNI', 'day(s)');
                    break;
                case 'hours':
                    startPeriodAgo = Uni.I18n.translate('period.hours', 'UNI', 'hour(s)');
                    break;
                case 'minutes':
                    startPeriodAgo = Uni.I18n.translate('period.minutes', 'UNI', 'minute(s)');
                    break;
            }

            switch (value.startTimeMode) {
                case 'ago':
                    startTimeMode = Uni.I18n.translate('general.period.ago', 'UNI', 'ago');
                    break;
                case 'ahead':
                    startTimeMode = Uni.I18n.translate('general.period.ahead', 'UNI', 'ahead');
                    break;
            }

            str += value.startAmountAgo + ' ' + startPeriodAgo + ' ' + startTimeMode;
            switch(value.startPeriodAgo) {
                case 'months':
                    if (value.onDayOfMonth) {
                        str += ' ' + Uni.I18n.translate('general.on', 'TME', 'on') + ' ' + Uni.I18n.translate('general.day', 'TME', 'day') +  ' ' + value.onDayOfMonth + ' ' + Uni.I18n.translate('general.ofTheMonth', 'TME', 'of the month');
                    } else {
                        str += ' ' + Uni.I18n.translate('general.onCurrentDayOfTheMonth', 'TME', 'on current day of the month');
                    }
                    str += ' ' + Uni.I18n.translate('general.at', 'TME', 'at') + ' ' + hoursMinutes;
                    break;
                case 'weeks':
                    var dayOfWeek;
                    switch (value.onDayOfWeek) {
                        case 1:
                            dayOfWeek = Uni.I18n.translate('general.day.monday', 'UNI', 'Monday');
                            break;
                        case 2:
                            dayOfWeek = Uni.I18n.translate('general.day.tuesday', 'UNI', 'Tuesday');
                            break;
                        case 3:
                            dayOfWeek = Uni.I18n.translate('general.day.wednesday', 'UNI', 'Wednesday');
                            break;
                        case 4:
                            dayOfWeek = Uni.I18n.translate('general.day.thursday', 'UNI', 'Thursday');
                            break;
                        case 5:
                            dayOfWeek = Uni.I18n.translate('general.day.friday', 'UNI', 'Friday');
                            break;
                        case 6:
                            dayOfWeek = Uni.I18n.translate('general.day.saturday', 'UNI', 'Saturday');
                            break;
                        case 7:
                            dayOfWeek = Uni.I18n.translate('general.day.sunday', 'UNI', 'Sunday');
                            break;
                    }
                    str += ' ' + Uni.I18n.translate('general.on', 'TME', 'on') + ' ' + dayOfWeek + ' ' + Uni.I18n.translate('general.at', 'TME', 'at') + ' ' + hoursMinutes;
                    break;
                case 'days':
                    str += ' ' + Uni.I18n.translate('general.at', 'TME', 'at') + ' ' + hoursMinutes;
                    break;
                case 'hours':
                    str += ' ' + Uni.I18n.translate('general.at', 'TME', 'at') + ' ' + (value.atMinute ? value.atMinute : '00') + ' ' + Uni.I18n.translate('period.minutes', 'UNI', 'minute(s)');
                    break;
            }
        }
        return str;
    }
});