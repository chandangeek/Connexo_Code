/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.override.Date', {
    override: 'Ext.Date',

    dayNames: [
        Uni.I18n.translate('general.day.sunday', 'UNI', 'Sunday'),
        Uni.I18n.translate('general.day.monday', 'UNI', 'Monday'),
        Uni.I18n.translate('general.day.tuesday', 'UNI', 'Tuesday'),
        Uni.I18n.translate('general.day.wednesday', 'UNI', 'Wednesday'),
        Uni.I18n.translate('general.day.thursday', 'UNI', 'Thursday'),
        Uni.I18n.translate('general.day.friday', 'UNI', 'Friday'),
        Uni.I18n.translate('general.day.saturday', 'UNI', 'Saturday')
    ],

    monthNames: [
        Uni.I18n.translate('general.month.january', 'UNI', 'January'),
        Uni.I18n.translate('general.month.february', 'UNI', 'February'),
        Uni.I18n.translate('general.month.march', 'UNI', 'March'),
        Uni.I18n.translate('general.month.april', 'UNI', 'April'),
        Uni.I18n.translate('general.month.may', 'UNI', 'May'),
        Uni.I18n.translate('general.month.june', 'UNI', 'June'),
        Uni.I18n.translate('general.month.july', 'UNI', 'July'),
        Uni.I18n.translate('general.month.august', 'UNI', 'August'),
        Uni.I18n.translate('general.month.september', 'UNI', 'September'),
        Uni.I18n.translate('general.month.october', 'UNI', 'October'),
        Uni.I18n.translate('general.month.november', 'UNI', 'November'),
        Uni.I18n.translate('general.month.december', 'UNI', 'December')
    ]
});

Ext.onReady(function () {
    var monthNumbers = [];

    monthNumbers[Uni.I18n.translate('general.month.january', 'UNI', 'January')] = 0;
    monthNumbers[Uni.I18n.translate('general.month.february', 'UNI', 'February')] = 1;
    monthNumbers[Uni.I18n.translate('general.month.march', 'UNI', 'March')] = 2;
    monthNumbers[Uni.I18n.translate('general.month.april', 'UNI', 'April')] = 3;
    monthNumbers[Uni.I18n.translate('general.month.may', 'UNI', 'May')] = 4;
    monthNumbers[Uni.I18n.translate('general.month.june', 'UNI', 'June')] = 5;
    monthNumbers[Uni.I18n.translate('general.month.july', 'UNI', 'July')] = 6;
    monthNumbers[Uni.I18n.translate('general.month.august', 'UNI', 'August')] = 7;
    monthNumbers[Uni.I18n.translate('general.month.september', 'UNI', 'September')] = 8;
    monthNumbers[Uni.I18n.translate('general.month.october', 'UNI', 'October')] = 9;
    monthNumbers[Uni.I18n.translate('general.month.november', 'UNI', 'November')] = 10;
    monthNumbers[Uni.I18n.translate('general.month.december', 'UNI', 'December')] = 11;

    monthNumbers[Uni.I18n.translate('general.month.january', 'UNI', 'January').substr(0, 3)] = 0;
    monthNumbers[Uni.I18n.translate('general.month.february', 'UNI', 'February').substr(0, 3)] = 1;
    monthNumbers[Uni.I18n.translate('general.month.march', 'UNI', 'March').substr(0, 3)] = 2;
    monthNumbers[Uni.I18n.translate('general.month.april', 'UNI', 'April').substr(0, 3)] = 3;
    monthNumbers[Uni.I18n.translate('general.month.may', 'UNI', 'May').substr(0, 3)] = 4;
    monthNumbers[Uni.I18n.translate('general.month.june', 'UNI', 'June').substr(0, 3)] = 5;
    monthNumbers[Uni.I18n.translate('general.month.july', 'UNI', 'July').substr(0, 3)] = 6;
    monthNumbers[Uni.I18n.translate('general.month.august', 'UNI', 'August').substr(0, 3)] = 7;
    monthNumbers[Uni.I18n.translate('general.month.september', 'UNI', 'September').substr(0, 3)] = 8;
    monthNumbers[Uni.I18n.translate('general.month.october', 'UNI', 'October').substr(0, 3)] = 9;
    monthNumbers[Uni.I18n.translate('general.month.november', 'UNI', 'November').substr(0, 3)] = 10;
    monthNumbers[Uni.I18n.translate('general.month.december', 'UNI', 'December').substr(0, 3)] = 11;
    /*
    monthNumbers[Uni.I18n.translate('general.month.short.january', 'UNI', 'Jan')] = 0;
    monthNumbers[Uni.I18n.translate('general.month.short.february', 'UNI', 'Feb')] = 1;
    monthNumbers[Uni.I18n.translate('general.month.short.march', 'UNI', 'Mar')] = 2;
    monthNumbers[Uni.I18n.translate('general.month.short.april', 'UNI', 'Apr')] = 3;
    monthNumbers[Uni.I18n.translate('general.month.short.may', 'UNI', 'May')] = 4;
    monthNumbers[Uni.I18n.translate('general.month.short.june', 'UNI', 'Jun')] = 5;
    monthNumbers[Uni.I18n.translate('general.month.short.july', 'UNI', 'Jul')] = 6;
    monthNumbers[Uni.I18n.translate('general.month.short.august', 'UNI', 'Aug')] = 7;
    monthNumbers[Uni.I18n.translate('general.month.short.september', 'UNI', 'Sep')] = 8;
    monthNumbers[Uni.I18n.translate('general.month.short.october', 'UNI', 'Oct')] = 9;
    monthNumbers[Uni.I18n.translate('general.month.short.november', 'UNI', 'Nov')] = 10;
    monthNumbers[Uni.I18n.translate('general.month.short.december', 'UNI', 'Dec')] = 11;
*/
    Ext.Date.monthNumbers = monthNumbers;
});