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
    ]
});

Ext.onReady(function () {
    var monthNumbers = [],
        january = Uni.I18n.translate('general.month.january', 'UNI', 'January'),
        february = Uni.I18n.translate('general.month.february', 'UNI', 'February'),
        march = Uni.I18n.translate('general.month.march', 'UNI', 'March'),
        april = Uni.I18n.translate('general.month.april', 'UNI', 'April'),
        may = Uni.I18n.translate('general.month.may', 'UNI', 'May'),
        june = Uni.I18n.translate('general.month.june', 'UNI', 'June'),
        july = Uni.I18n.translate('general.month.july', 'UNI', 'July'),
        august = Uni.I18n.translate('general.month.august', 'UNI', 'August'),
        september = Uni.I18n.translate('general.month.september', 'UNI', 'September'),
        october = Uni.I18n.translate('general.month.october', 'UNI', 'October'),
        november = Uni.I18n.translate('general.month.november', 'UNI', 'November'),
        december = Uni.I18n.translate('general.month.december', 'UNI', 'December');

    monthNumbers[january] = 0;
    monthNumbers[february] = 1;
    monthNumbers[march] = 2;
    monthNumbers[april] = 3;
    monthNumbers[may] = 4;
    monthNumbers[june] = 5;
    monthNumbers[july] = 6;
    monthNumbers[august] = 7;
    monthNumbers[september] = 8;
    monthNumbers[october] = 9;
    monthNumbers[november] = 10;
    monthNumbers[december] = 11;

    monthNumbers[january.substr(0, 3)] = 0;
    monthNumbers[february.substr(0, 3)] = 1;
    monthNumbers[march.substr(0, 3)] = 2;
    monthNumbers[april.substr(0, 3)] = 3;
    monthNumbers[may.substr(0, 3)] = 4;
    monthNumbers[june.substr(0, 3)] = 5;
    monthNumbers[july.substr(0, 3)] = 6;
    monthNumbers[august.substr(0, 3)] = 7;
    monthNumbers[september.substr(0, 3)] = 8;
    monthNumbers[october.substr(0, 3)] = 9;
    monthNumbers[november.substr(0, 3)] = 10;
    monthNumbers[december.substr(0, 3)] = 11;

    Ext.Date.monthNumbers = monthNumbers;
    Ext.Date.monthNames = [january, february, march, april, may, june, july, august, september, october, november, december];
});